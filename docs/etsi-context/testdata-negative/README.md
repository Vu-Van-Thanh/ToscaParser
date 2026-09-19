# Negative fixture packages

Each directory here is a VNF package that breaks **exactly one** normative requirement. They share
one minimal, otherwise-conformant descriptor, so the only difference between a package and its
neighbours is the violation itself — which is what makes a finding attributable when one fires.

Every package carries a `.mf` manifest, a `ChangeLog.txt` and a conformant `vnfm_info`, so C13 and
TOSCA03 stay quiet and the intended rule is the only thing in the output.

Each `Definitions/descriptor.yaml` opens with a comment stating the clause violated, why it matters
and what the parse is expected to produce.

**Only `HybridWebCnf2_missing_mciop` is asserted on by the test suite** (`FindingsTest`). The other
eleven are checked by hand, through the `etsi-vnfd-parser-demo` project: `GET /parse-all` prints the
rules each one raised. They live here rather than under `src/test/resources/` for that reason - and
because each ships its own copy of the ETSI type definitions, which would otherwise be several
megabytes re-copied into `target/` on every build.

| Directory | Rule | Requirement broken |
|---|---|---|
| `neg_C1_no_workload` | C1 | IFA011 7.1.2.2 Note 6 — neither `osContainerDesc` nor `mciopId` has an element |
| `HybridWebCnf2_missing_mciop` | C2 | IFA011 7.1.6.2.2 Note 10 — a VDU with no container requirement and no associated MCIOP |
| `neg_C4_two_helm_charts` | C4 | SOL001 6.8.14.7 — two `HelmChart` artifacts on one `Mciop` |
| `neg_C5_rule_without_script` | C5 | SOL001 6.8.14.7 — a `HelmParamMappingRule` with no script to consume it |
| `neg_C6_no_swimage` | C6 | SOL001 6.8.12.6 — a `Vdu.OsContainer` with no `SwImage` |
| `neg_C6_two_swimages` | C6 | SOL001 6.8.12.6 — a `Vdu.OsContainer` with two |
| `neg_C8_mciop_no_vdu` | C8 | SOL001 6.8.14.6 — `associatedVdu` occurrences are `[1, UNBOUNDED]` |
| `neg_C12_levels_no_default` | C12 | IFA011 7.1.8.2.2 — two instantiation levels, no default named |
| `neg_C19_no_mcio_id_data` | C19 **and** TOSCA02 | IFA011 7.1.6.2.2 — see the note below |
| `neg_C24_script_without_event` | C24 | IFA011 7.1.13.2 NOTE 1 — a lifecycle script with neither `event` nor `lcmTransitionEvent` |
| `neg_TOSCA02_missing_required` | TOSCA02 | TOSCA 1.3 cl. 3.6.2 — a `required: true` property unassigned |
| `neg_zipslip_artifact` | *(throws)* | `[PROJECT-SPECIFIC]` — an artifact path climbing out of the package |

## Two results that are not a clean single rule, on purpose

**`neg_C19_no_mcio_id_data` fires two errors, C19 and TOSCA02.** The ETSI type definitions file
states no `required` key for `mcio_identification_data`, and TOSCA Simple Profile YAML 1.3 clause
3.6.2 gives `required` the default `true` — so the generic check, which reads the type file and
needs no Java, already catches this. C19 restates it from IFA011 prose. Within the CNF-only scope of
this library the two conditions coincide exactly. Worth knowing before adding more hand-written
rules that a type file already covers.

**`neg_zipslip_artifact` does not produce a finding at all — the parse throws**
`PackageReader.CsarSecurityException`. That is deliberate: a finding would mean the file had already
been read. This is the one case where refusing beats reporting.

## No fixture for C28

C28 (IFA011 7.1.8.2.2, a flavour referencing an MCIOP-based workload while declaring no
`mciopProfile`) is effectively unreachable. `MCIOP_CISM` requires an `Mciop` associated with the VDU
in the same flavour, and every `Mciop` of a flavour produces an `MciopProfile` — so the two
conditions cannot both hold. Writing a fixture would mean inventing a descriptor shape the parser
cannot produce. Recorded here instead.

## Checking them

From the test suite, or through the `etsi-vnfd-parser-demo` project, which keeps its own copy of
these packages under `packages/negative/`:

```
curl "localhost:8080/findings?pkg=neg_C4_two_helm_charts&pretty=1"
curl "localhost:8080/parse-all"        # one summary row per package, both kinds
```
