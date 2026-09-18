# ExampleCorp_FunctionsCnf_vnf_pkg

Every TOSCA function SOL001 allows, plus a conformant and a non-conformant scalar unit. The point is
not that values come out right — it is that the parser **says what it could and could not evaluate**
instead of silently dropping what it cannot.

`[ETSI NORMATIVE]` SOL001 V5.4.1 clause 5.9 Table 5.9-1 lists the permitted functions:
`get_property`, `get_artifact`, `get_input`, `get_attribute`, and the intrinsic `concat`, `join`,
`token`. `get_operation_output` and `get_nodes_of_type` are **not** among them.

Table 5.9-2 restricts where `get_input` may appear — `VNF.flavour_id`, `VNF.modifiable_attributes`,
`VNF.configurable_properties`, `Vdu.Compute.configurable_properties`. Uses outside that table are
kept and tagged rather than rejected: the library describes a descriptor, it does not correct one.

## Actual parse result

| Property | Function | Result |
|---|---|---|
| `VNF.flavour_id` | `get_input` | `df[0].flavourId` = **`default`**, from the input's declared default |
| `AppVdu.vdu_profile.min_number_of_instances` | `get_input` | `INPUT_BOUND` |
| `AppCp.description` | `get_attribute` | `RUNTIME_BOUND` |
| `AppVdu.name` | `concat` over literals | **resolved** `"app-vdu"` |
| `AppVdu.description` | `concat` with a `get_input` | `INPUT_BOUND`, argument tree kept |
| `AppContainer.name` | `get_property` onto a `concat` | **resolved** `"app-vdu"` |
| `sw_image.version` | `join` over a literal list | **resolved** `"1.0.0"` |
| `OpsCp.description` | `token` over literals | **resolved** `"south"` |
| `requested_memory_resources: 64 MiB` | — | `{text: "64 MiB", bytes: 67108864}` |
| `memory_resource_limit: "128MB"` | — | parsed, plus one `WARN TOSCA01` |

Three rules decide all of it:

- a function whose arguments are all values is evaluated, because TOSCA 1.3 clause 4.3 defines
  `concat`, `join` and `token` over their arguments alone and `get_property` over the descriptor;
- a function with a deferred argument takes **that argument's** resolution, so
  `concat: [ "tag-", { get_input: x } ]` is input-bound rather than unresolvable;
- `get_input` and `get_attribute` are never evaluated - no VNF instance exists at parse time - and
  the argument tree is kept either way, so a consumer can see what is missing.

`flavourId` is the one place an input is read for a value rather than deferred: IFA011 clause
7.1.8.2.2 makes it M,1, so a flavour cannot be left unnamed. The input's declared `default` is used,
and rule C29 reports it when even that is absent. Nothing is substituted from outside the package.

## No gaps left here

Both problems this package originally exposed - nothing being evaluated statically, and a
`get_input` flavour_id yielding an empty identifier - are fixed.
