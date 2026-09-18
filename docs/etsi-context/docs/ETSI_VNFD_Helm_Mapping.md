# TOSCA/VNFD package with Helm support — ETSI NFV V5.4.1 design

**Target standard:** ETSI NFV Release 5, V5.4.1 (SOL001, SOL018, IFA011 as primary; SOL003/SOL005 for package-structure evidence)
**Package delivered:** `ExampleCorp_SimpleWebCnf_vnf_pkg.zip`

## 1. Direct answer

The package models a containerized VNF ("SimpleWebCnf") whose single VDU is realized **entirely by a Helm chart** — no `Vdu.OsContainer` node is used, following the "simplified design by using MCIOP" pattern in ETSI GS NFV-SOL 001 V5.4.1 Annex A.23. The Helm chart is attached as a `tosca.artifacts.nfv.HelmChart` artifact on a `tosca.nodes.nfv.Mciop` node template, which is associated to a `tosca.nodes.nfv.Vdu.OsContainerDeployableUnit` node representing the VNFC.

## 2. Primary ETSI specifications and why

| Concern | Primary spec | Why |
|---|---|---|
| TOSCA node/artifact types (`Vdu.OsContainerDeployableUnit`, `Mciop`, `HelmChart`, `HelmParamMappingScript/Rule`) | **SOL001 V5.4.1** clauses 6.3.3–6.3.5, 6.8.13, 6.8.14 | SOL001 is the sole normative source for ETSI NFV TOSCA types. |
| VNFD information-model semantics of the MCIOP (`MciopProfile`) | **IFA011 V5.4.1** clause 7.1.8.20 | IFA011 owns the VNFD information element; SOL001's `Mciop` node type text itself says it "does not correspond to an information element defined in ETSI GS NFV-IFA 011" but is "capable of being profiled by the properties of the MciopProfile information element" — i.e. IFA011 governs the semantic profile, SOL001 the TOSCA node. |
| Helm chart structure / CISM workload operations | **SOL018 V5.4.1** clauses 4.2.2.3.1, 7.4.2–7.4.6 | SOL018 profiles the Helm CLI against the IFA040 container-workload-management interface; it is the authority for what a Helm chart/MCIOP means operationally, not SOL001. |
| VNF package directory structure (`TOSCA.meta`, `Definitions/`, `Artifacts/`) | **SOL003 V5.4.1** cl. 10.4.4.3.2 / **SOL005 V5.4.1** cl. 9.4.4.3.2 | These clauses (describing how the NFVO returns a VNFD ZIP) are the only package-structure evidence available in the provided sources; the full CSAR/manifest format is SOL004, which is **not** among the provided ETSI sources. |

## 3. TOSCA representation — verified element by element

| YAML element | Type / clause (SOL001 V5.4.1) | Verified against |
|---|---|---|
| `WebVdu` | `tosca.nodes.nfv.Vdu.OsContainerDeployableUnit` — cl. 6.8.13 | Properties used (`name`, `description`, `vdu_profile`, `mcio_identification_data`, `is_num_of_instances_cluster_based`) all appear in Table 6.8.13.2-1 |
| `WebCp` | `tosca.nodes.nfv.VduCp` — referenced from cl. 6.8.13.5 (`associable` capability) | `virtual_binding` requirement pattern per VDU/VduCp examples |
| `web_mciop` | `tosca.nodes.nfv.Mciop` — cl. 6.8.14 | `associatedVdu` requirement (Table 6.8.14.4-1); target node type constrained to `Vdu.OsContainerDeployableUnit` (cl. 6.8.14.6) |
| `web_helm_chart` artifact | `tosca.artifacts.nfv.HelmChart` — cl. 6.3.3 | `derived_from: tosca.artifacts.File`, `file_ext: [tar, tar.gz, tgz]`; **max one per Mciop node template** (cl. 6.8.14.7) — satisfied |
| `web_param_mapping_script` artifact | `tosca.artifacts.nfv.HelmParamMappingScript` — cl. 6.3.4 | Required property `language` set to `bash` (valid value per Table 6.3.4.2-1) |
| `web_param_mapping_rule` artifact | `tosca.artifacts.nfv.HelmParamMappingRule` — cl. 6.3.5 | Present only alongside the script artifact, per the cl. 6.8.14.7 constraint ("If there is no artifact definition of type HelmParamMappingScript there shall be no artifact definition of type HelmParamMappingRule") |

**[ETSI NORMATIVE]** Clause 6.8.13.7: *"In case a node template of type tosca.nodes.nfv.Vdu.OsContainerDeployableUnit is present in a VNFD service template, while no node template of type tosca.nodes.nfv.Vdu.OsContainer is present, at least one node template of type tosca.nodes.nfv.Mciop shall be present in the VNFD service template."* — satisfied by `web_mciop`.

**[ETSI NORMATIVE]** Clause 6.8.14.7: at most one `HelmChart` artifact, at most one `HelmParamMappingScript`, at most one `HelmParamMappingRule` per `Mciop` node template — all three constraints satisfied (exactly one of each, in `web_mciop`).

## 4. VNFD information-model mapping (IFA011 ↔ TOSCA ↔ Helm)

| IFA011 `MciopProfile` attribute (cl. 7.1.8.20.2) | Cardinality | TOSCA representation in this package | Helm / SOL018 correspondence |
|---|---|---|---|
| `mciopId` | M, 1 | Node template name `web_mciop` (identifies the MCIOP in the VNF package) | Identifies which chart is the MCIOP referenced by `{CHART}` in `helm install/upgrade` (SOL018 cl. 7.4.2/7.4.3) |
| `associatedVdu` | M, 0..N | `requirements: [ associatedVdu: WebVdu ]` | Determines which VNFC/Pod-producing MCIO the chart's rendered manifests belong to |
| `mciopParameterMappingRule` | M, 0..1 | `web_param_mapping_rule` artifact (`tosca.artifacts.nfv.HelmParamMappingRule`) | Interpreted by the mapping script before invoking Helm |
| `lcmOpParameterMappingScriptId` | M, 0..1 | `web_param_mapping_script` artifact (`tosca.artifacts.nfv.HelmParamMappingScript`) | Produces the effective `values.yaml` passed to `helm install`/`helm upgrade` |
| `deploymentOrder`, `affinityOrAntiAffinityGroupId` | M, 0..1 / 0..N | **Not used** in this single-MCIOP example | `NOT SPECIFIED IN PROVIDED ETSI SOURCES` beyond the attribute definitions themselves — no multi-MCIOP ordering/affinity is modeled here |

**[ETSI SEMANTIC]** IFA011 cl. 7.1.8.20.1 defines the MCIOP itself: *"A Managed Container Infrastructure Object Package (MCIOP) is a hierarchical aggregate of information objects for OS container management and orchestration."* SOL018 cl. 5.2 (referenced from cl. 7.1) identifies the Helm chart as the MCIOP for the Helm-profiled solution — i.e. **"MCIOP" is the ETSI information-model concept; "Helm chart" is its implementation realization**, which is why the artifact type is named `HelmChart` rather than "MCIOP file."

**[ETSI NORMATIVE]** IFA011 requirements `VNF_PACK.DESC.012` ("The VNF Package shall contain one or more MCIOPs … when the VNF is realized by a set of OS containers") and `VNF_PACK.META.028` ("The VNFD shall support the possibility to reference one or more MCIOP(s)") are the package-level and descriptor-level normative basis for including the Helm chart artifact in the VNFD at all.

## 5. Helm chart structure (SOL018 conformance)

**[ETSI SEMANTIC → HELM IMPLEMENTATION]** SOL018 cl. 4.2.2.3.1 documents the expected Helm chart file structure (`Chart.yaml`, `values.yaml`, `templates/`, `charts/`, `crds/`, `templates/NOTES.txt`, etc.). `Artifacts/Charts/simple-web-cnf/` in the delivered package follows that structure (`Chart.yaml`, `values.yaml`, `templates/deployment.yaml`, `templates/service.yaml`, `templates/_helpers.tpl`, `templates/NOTES.txt`), then is packaged as `simple-web-cnf-1.0.0.tgz` — matching the `file_ext: [tar, tar.gz, tgz]` constraint on `tosca.artifacts.nfv.HelmChart` (SOL001 cl. 6.3.3).

**[MANO INTERPRETATION]** At VNF instantiation, the VNFM sends a request that (per SOL018 cl. 7.3.1/7.4.2) the CISM realizes as `helm install {RELEASE} {CHART}`, where `{CHART}` resolves to this MCIOP/chart artifact and `{RELEASE}` is CISM/VNFM-assigned; scale/heal map to `helm upgrade`/`helm rollback` (cl. 7.4.3/7.4.4), termination to `helm uninstall` (cl. 7.4.5).

**[KUBERNETES IMPLEMENTATION]** The chart's `Deployment` resource is what SOL018 clause 6.2.2.1-5 (Container-related API mapping) and Table 6.2.2.1-5 (Container `resources.limits.cpu`/`memory` ↔ `Vdu.OsContainer.cpu_resource_limit`/`memory_resource_limit`) describe when a `Vdu.OsContainer` node **is** used — since this example intentionally omits `Vdu.OsContainer` (Annex A.23 pattern), those specific field-level mappings do not apply here; the container resource limits live only inside the Helm chart's own `values.yaml`/templates, entirely outside VNFD scope. This is called out explicitly so it is not mistaken for an ETSI-mandated mapping.

## 6. Compliance table

| Requirement | ETSI Spec | Clause | Normative level | Evidence | Status |
|---|---|---|---|---|---|
| VNFD must include a `Vdu.OsContainerDeployableUnit`-compatible MCIOP when no `Vdu.OsContainer` is used | SOL001 | 6.8.13.7 | SHALL | `web_mciop` present, associated to `WebVdu` | PASS |
| `Mciop` node template has at most one `HelmChart` artifact | SOL001 | 6.8.14.7 | SHALL (max 1) | Exactly one (`web_helm_chart`) | PASS |
| `HelmParamMappingRule` only present alongside `HelmParamMappingScript` | SOL001 | 6.8.14.7 | SHALL | Both present together | PASS |
| `HelmParamMappingScript.language` uses a valid value | SOL001 | 6.3.4.2 | Constraint | `language: bash` (valid: `bash`, `python`) | PASS |
| VNF Package contains ≥1 MCIOP when VNF realized by OS containers | IFA011 | VNF_PACK.DESC.012 | SHALL | One MCIOP (`web_mciop`) in the package | PASS |
| VNFD references the MCIOP(s) used in containerized workload management | IFA011 | VNF_PACK.META.028 | SHALL | `web_mciop` referenced in topology | PASS |
| Full CSAR/manifest structure conformance (SOL004) | SOL004 | — | — | Not among provided ETSI sources | CANNOT VERIFY |
| Helm chart CLI major version compatibility | SOL018 | 7.2 | SHALL (Helm CLI major version "3") | Not asserted by this package (deployment-environment property, not a VNFD artifact) | NOT SPECIFIED |

## 7. Traceability matrix

| Requirement | ETSI Concept | Spec | Clause | TOSCA | MANO | Kubernetes | Implementation |
|---|---|---|---|---|---|---|---|
| Deploy WebVdu as a containerized workload | MCIOP | IFA011 | 7.1.8.20 | `tosca.nodes.nfv.Mciop` (`web_mciop`) | VNFM issues LCM request → CISM Helm interface | `Deployment` rendered from chart | `helm install` (SOL018 7.4.2) |
| Attach Helm chart to MCIOP | HelmChart artifact | SOL001 | 6.3.3 | `web_helm_chart` artifact on `web_mciop` | Chart resolved as `{CHART}` | n/a | `simple-web-cnf-1.0.0.tgz` |
| Map LCM parameters into Helm values | HelmParamMappingScript/Rule | SOL001 / IFA011 | 6.3.4/6.3.5, 7.1.20 | `web_param_mapping_script` / `web_param_mapping_rule` | Script runs in VNFM execution environment before CISM call | Rendered `values.yaml` | `lcm_param_mapping.sh` |
| Expose WebVdu connectivity | VDU-CP association | SOL001 | 6.8.13.5 | `WebCp` → `virtual_binding: WebVdu` | n/a (VNFD-internal) | `Service` (chart) | `[IMPLEMENTATION MAPPING]` |

## 8. Assumptions and project-specific decisions

- **[ASSUMPTION]** `descriptor_id`, `mciop` naming, chart repository/image names, and the mapping-rule file syntax are example values for this deliverable, not ETSI-defined values.
- **[PROJECT-SPECIFIC]** The choice to omit `Vdu.OsContainer` (Annex A.23 "simplified design") rather than also modeling per-container CPU/memory/image inside the VNFD is a design choice; both patterns are ETSI-valid.
- **[IMPLEMENTATION MAPPING]** `TOSCA.meta` header fields beyond `Entry-Definitions`/`Other-Definitions` (e.g. `CSAR-Version`, `Created-By`) follow common OASIS-CSAR/ETSI SOL004 convention but could not be verified against SOL004, which is outside the provided sources — `CANNOT VERIFY FROM PROVIDED ETSI SOURCES` for their exact required values.
- **Note on SOL001 internal naming:** SOL001 Annex A examples (clause A.9-area VNFD text) use artifact type names `tosca.artifacts.nfv.HelmMappingScript` / `HelmMappingRule` in one illustrative example, while the normative clauses 6.3.4/6.3.5 define `tosca.artifacts.nfv.HelmParamMappingScript` / `HelmParamMappingRule`. This package uses the **clause 6.3.4/6.3.5 normative names**, since a normative type-definition clause takes precedence over an informative annex example; flagging this discrepancy per the no-hallucination/self-audit requirement rather than silently picking one.

## 9. Self-audit

- Used SOL001 as primary for all TOSCA types; IFA011 for MCIOP information-model semantics; SOL018 for Helm/CISM operational meaning — correctly prioritized. ✔
- All type/property names verified against retrieved clause text (6.3.3–6.3.5, 6.8.13, 6.8.14, IFA011 7.1.8.20). No fabricated clause numbers. ✔
- SHALL-level constraints (max 1 HelmChart, script/rule pairing, cl. 6.8.13.7 fallback rule) preserved as SHALL. ✔
- Kubernetes `Deployment`/`Service` objects and Helm chart internals labeled `[KUBERNETES IMPLEMENTATION]`/`[HELM IMPLEMENTATION]`, never presented as ETSI requirements. ✔
- SOL004 (VNF package/CSAR format) is not in the provided source set — flagged explicitly wherever package-level structure beyond `TOSCA.meta`'s `Entry-Definitions`/`Other-Definitions` was relevant. ✔
