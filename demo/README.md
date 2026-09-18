# etsi-vnfd-parser-demo

A Spring Boot application that consumes `etsi-vnfd-parser` **as a published artifact** and serves the
parsed VNFD as JSON. It reaches into nothing: if something here needs library internals, that is a
signal the library's public surface is wrong.

## Run it

```bash
# once, from the repository root: publish the library to the local repository
mvn -DskipTests install

# then, from this directory
cd demo
mvn spring-boot:run                                              # http://localhost:8080
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9000
```

Or build the fat jar:

```bash
mvn package
java -jar target/etsi-vnfd-parser-demo-0.1.0-SNAPSHOT.jar
java -jar target/etsi-vnfd-parser-demo-0.1.0-SNAPSHOT.jar --server.port=9000
```

Sources change in the library? Re-run `mvn -DskipTests install` at the repository root, otherwise
this app keeps resolving the previously installed jar.

## Two version overrides a host application will need as well

`demo/pom.xml` pins two dependencies above what Spring Boot 2.7 manages:

| Dependency | Boot 2.7 manages | Needed | Why |
|---|---|---|---|
| `snakeyaml` | 1.30 | **2.2** | `LoaderOptions.setNestingDepthLimit(int)` arrived in 2.x; the parser calls it to bound document nesting. Without the override the first package read fails with `NoSuchMethodError`. |
| `jackson-bom` | 2.13.x | **2.17.2** | The library is compiled against 2.17.2; one Jackson on the classpath rather than two. |

This is the kind of thing the demo exists to find. Both were hit here before anything else worked.

## Endpoints

| Request | Returns |
|---|---|
| `GET /health` | where the packages were found and how many |
| `GET /packages` | every bundled package, `kind: positive \| negative` |
| `GET /parse?pkg=<name>` | `{ package, hasErrors, vnfd, findings }` — the main one |
| `GET /parse?dir=<path>` | the same for any package directory on disk |
| `GET /vnfd?pkg=<name>` | the VNFD alone, for diffing one run against another |
| `GET /findings?pkg=<name>` | findings alone, with counts and clauses |
| `GET /debug?pkg=<name>` | the TOSCA layer: templates, types, and **unbound node templates** |
| `GET /parse-all` | one summary row per package |

Status codes carry the library's own distinction:

- **200** — the package parsed. Findings may still say it breaks a rule; whether that is fatal is
  the caller's decision, not the parser's.
- **422** — the package could not be read at all (broken YAML, missing `Entry-Definitions`, a path
  escaping the package root).
- **400** — no `pkg` or `dir` given, or an unknown package name.

### `/debug` is the one to reach for first

```bash
curl "localhost:8080/debug?pkg=ExampleCorp_VendorTypeCnf_vnf_pkg" | jq .unboundNodeTemplates
```

A node template whose type resolves to no ETSI ancestor is dropped by the binder **silently** — no
finding, no log line, it simply does not appear in the VNFD. That is the quietest way for a
descriptor to lose content, and `unboundNodeTemplates` is the only place it becomes visible.

## Debugging

Open `demo/pom.xml` as a project in the IDE and run `Main` in debug mode; breakpoints in the
controller and in `ParseApi` work immediately.

To step **into** the library, install its sources once:

```bash
cd ..
mvn source:jar install
```

Or open `ToscaParser` and `demo` as two modules in the same IDE window, which links the source
directly and skips the reinstall loop entirely.

Remote debugging, when running from the command line:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

Useful breakpoints:

| To understand | Break in |
|---|---|
| why a node template vanished | `NodeTypeResolver.resolve` (inside `NodeBinder.java`) |
| how a property became `INPUT_BOUND` | `TemplateReader.parsePropertyValue` |
| where a finding came from | `SpecRules`, or `ConstraintChecker.check` in `NodeBinder.java` |
| how one flavour was assembled | `DeploymentFlavourMapper.map` in `VnfdMappers.java` |
| file reading and path resolution | `PackageReader.readFileAndImports` |

## What is in here

| File | Role |
|---|---|
| `ParseApi.java` | the library called the way a host application would; **no HTTP anywhere in it** |
| `VnfdJson.java` | the serializer, written by hand — see below |
| `PackageCatalog.java` | finds packages under `docs/etsi-context/testdata/` and `src/test/resources/negative/` |
| `VnfdController.java` | the thin REST layer; makes no parsing decision of its own |
| `Main.java` | `@SpringBootApplication` |

Wiring the parser into your own service means copying `ParseApi` and `VnfdJson`. The controller is
an example, not a dependency.

### Why the serializer is hand-written

The model classes carry **no Jackson annotations**, and nearly every scalar getter returns
`Optional<PropertyValue<T>>` — a double wrapper no default serializer renders as anything a reader
would want. `VnfdJson` walks the getters instead, and registers serializers for `PropertyValue`,
`Literal`, `FunctionCall` and `Quantity` so a property reads the same nested inside a SOL001 datatype
as it does at the top level.

Conventions:

- an empty `Optional` is omitted rather than written as `null`;
- a resolved property is written as its plain value;
- an unresolved one becomes `{_fn, _args, _resolution, _raw}`, so a `get_input` is visibly deferred
  rather than silently missing;
- a `Quantity` keeps both what the descriptor wrote and the normalised byte count, and is flagged
  `_nonCanonicalSpacing` when it omits the space TOSCA requires;
- the IFA011 attribute `deploymentFlavour` is serialised as `df`, and anything outside IFA011 sits
  under `_extensions`.
