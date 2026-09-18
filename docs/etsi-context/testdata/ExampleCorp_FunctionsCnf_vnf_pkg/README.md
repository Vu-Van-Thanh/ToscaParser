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
| `VNF.flavour_id` | `get_input` | `df[0].flavourId` is **`""`** — see gap 2 |
| `AppVdu.vdu_profile.min_number_of_instances` | `get_input` | `INPUT_BOUND` |
| `AppCp.description` | `get_attribute` | `RUNTIME_BOUND` |
| `AppVdu.name` | `concat` over literals only | `UNRESOLVABLE` — see gap 1 |
| `AppVdu.description` | `concat` with a `get_input` | `UNRESOLVABLE` |
| `AppContainer.name` | `get_property` within the document | `UNRESOLVABLE` — see gap 1 |
| `sw_image.version` | `join` over a literal list | `UNRESOLVABLE` |
| `OpsCp.description` | `token` over literals | `UNRESOLVABLE` |
| `requested_memory_resources: 64 MiB` | — | `{text: "64 MiB", bytes: 67108864}` |
| `memory_resource_limit: "128MB"` | — | parsed, plus one `WARN TOSCA01` |

`get_input` and `get_attribute` behave exactly as intended: no VNF instance exists at parse time, so
neither can have a value, and both are tagged rather than dropped. The argument tree is kept, so a
consumer can see what it would take to resolve them.

Scalar units behave as intended too. TOSCA Simple Profile YAML 1.3 clause 3.3.6 spells a scalar-unit
`<scalar> <unit>`, with the space; `128MB` is readable but not conformant, and is reported without
being rejected. Exactly one TOSCA01 fires.

## Known gaps this package documents

1. **Nothing is ever statically evaluated.** `concat`, `join`, `token` and `get_property` come back
   `UNRESOLVABLE` even when every argument is a literal and the whole chain sits inside this one
   document. An expression-resolution stage was designed but never built, so any TOSCA function is
   treated as unresolvable regardless of its arguments.

2. **A `get_input` flavour_id produces an empty `flavourId`.** `VNF.flavour_id` is one of the four
   places Table 5.9-2 explicitly permits `get_input`, yet the flavour mapper reads only resolved
   values and falls back to `""`. A conformant descriptor therefore yields a nameless deployment
   flavour, with no finding to say so. This is the most consequential gap of the two.
