# etsi-vnfd-parser-demo

A separate Maven project that consumes `etsi-vnfd-parser` **as a published artifact** and serves the
parsed VNFD as JSON over HTTP. It reaches into nothing: if something here needs library internals,
that is a signal the library's public surface is wrong.

## Run it

```bash
# once, from the repository root: publish the library to the local repository
mvn -DskipTests install

# then, from this directory
cd demo
mvn compile exec:java                                    # server on http://localhost:8080
mvn compile exec:java -Dexec.args="--port 9000"
mvn compile exec:java -Dexec.args="ExampleCorp_HybridWebCnf2_vnf_pkg"        # one package, to stdout
mvn compile exec:java -Dexec.args="ExampleCorp_HybridWebCnf2_vnf_pkg --json"
```

`exec:java` does **not** recompile on its own — run `compile` in the same command or you will be
looking at the previous build.

Or build a jar with its dependencies beside it:

```bash
mvn package
java -jar target/etsi-vnfd-parser-demo-0.1.0-SNAPSHOT.jar --port 8080
```

## Endpoints

| Request | Returns |
|---|---|
| `GET /packages` | every bundled package, `kind: positive \| negative` |
| `GET /parse?pkg=<name>` | `{ package, hasErrors, vnfd, findings }` — the main one |
| `GET /parse?dir=<path>` | the same for any package directory on disk |
| `GET /vnfd?pkg=<name>` | the VNFD alone, for diffing one run against another |
| `GET /findings?pkg=<name>` | findings alone, with counts and clauses |
| `GET /debug?pkg=<name>` | the TOSCA layer: templates, types, and **unbound node templates** |
| `GET /parse-all` | one summary row per package |

Add `&pretty=1` to indent.

A package that cannot be read at all answers **422** with `{ok: false, errorType, error}`. A package
that reads but breaks a rule answers **200** — the findings carry the verdict. That distinction is
the library's own and is preserved here.

### `/debug` is the one to reach for first

It lists every node template with the ETSI type it resolved to, and separately lists the ones that
resolved to nothing:

```bash
curl "localhost:8080/debug?pkg=ExampleCorp_VendorTypeCnf_vnf_pkg&pretty=1"
```

A node template whose type resolves to no ETSI ancestor is dropped by the binder **silently** —
no finding, no log line, it simply does not appear in the VNFD. That is the quietest way for a
descriptor to lose content, and `unboundNodeTemplates` is the only place it becomes visible.

## Debugging into the library

Breakpoints in the demo work out of the box. To step **into** the parser, install its sources
alongside the jar:

```bash
cd ..
mvn source:jar install
```

Then open `demo/pom.xml` as a project in the IDE and run `Main` in debug mode. Useful places to
break:

| To understand | Break in |
|---|---|
| why a node template vanished | `NodeTypeResolver.resolve` (inside `NodeBinder.java`) |
| how a property became `INPUT_BOUND` | `TemplateReader.parsePropertyValue` |
| where a finding came from | `SpecRules`, or `ConstraintChecker.check` in `NodeBinder.java` |
| how one flavour was assembled | `DeploymentFlavourMapper.map` in `VnfdMappers.java` |

## What is in here

| File | Role |
|---|---|
| `ParseApi.java` | the library called the way a host application would; no HTTP anywhere in it |
| `VnfdJson.java` | the serializer, written by hand — see below |
| `PackageCatalog.java` | finds packages under `docs/etsi-context/testdata/` and `src/test/resources/negative/` |
| `VnfdHttpServer.java` | the JDK's built-in HTTP server, so the demo adds no framework |
| `Main.java` | entry point; no arguments starts the server, an argument parses one package |

### Why the serializer is hand-written

The model classes carry **no Jackson annotations**, and nearly every scalar getter returns
`Optional<PropertyValue<T>>` — a double wrapper no default serializer renders as anything a reader
would want. `VnfdJson` walks the getters instead, and registers serializers for `PropertyValue` and
`Quantity` so a property reads the same nested inside a SOL001 datatype as it does at the top level.

Conventions:

- an empty `Optional` is omitted rather than written as `null`;
- a resolved property is written as its plain value;
- an unresolved one becomes `{_fn, _args, _resolution, _raw}`, so a `get_input` is visibly deferred
  rather than silently missing;
- a `Quantity` keeps both what the descriptor wrote and the normalised byte count, and is flagged
  `_nonCanonicalSpacing` when it omits the space TOSCA requires;
- the IFA011 attribute `deploymentFlavour` is serialised as `df`, and anything outside IFA011 sits
  under `_extensions`.
