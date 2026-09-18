# Does ETSI NFV support Helm in the VNFD, and how? Combined OsContainer + Helm design and parsed VNFD model

**Target standard:** ETSI NFV Release 5, V5.4.1 (SOL001, SOL018, IFA011 primary; SOL003 for grant/parsing evidence)
**Package delivered:** `ExampleCorp_HybridWebCnf_vnf_pkg.zip`
**Companion:** `ExampleCorp_HybridWebCnf_vnf_pkg_PARSED_VNFD.json` (illustrative parsed VNFD instance)

## 1. Direct answer: yes, ETSI NFV V5.4.1 explicitly supports Helm for the VNFD

Helm is supported, but not as a first-class ETSI concept — it is the **profiled implementation of the MCIOP (Managed Container Infrastructure Object Package)**, an ETSI-defined VNFD concept. The chain of evidence:

- **[ETSI SEMANTIC]** IFA011 cl. 7.1.8.20.1: *"A Managed Container Infrastructure Object Package (MCIOP) is a hierarchical aggregate of information objects for OS container management and orchestration."* The **MciopProfile** information element (cl. 7.1.8.20) is a first-class part of the VNFD's deployment-flavour model (`VnfDf.mciopProfile`, cl. 7.1.8.2.2): *"This attribute shall be present if the DF references (via the vduProfile) containerized workloads based on a MCIOP."*
- **[ETSI NORMATIVE — implementation profiling]** SOL018 cl. 1 (Scope): *"It profiles the reference Helm™ documentation as NFV protocol and data model solution for management of OS container workload based on an MCIOP."* SOL018 cl. 4.2.2.1: *"Helm™ is a tool for managing OS container workloads deployed on Kubernetes® CIS clusters based on MCIOPs called Helm™ charts."* — **the Helm chart *is* the MCIOP**, for deployments that use the Helm-profiled CISM interface.
- **[TOSCA REPRESENTATION]** SOL001 gives the MCIOP concept a TOSCA node (`tosca.nodes.nfv.Mciop`, cl. 6.8.14) and a Helm-specific artifact type (`tosca.artifacts.nfv.HelmChart`, cl. 6.3.3), plus two supporting artifact types for parameter mapping (`HelmParamMappingScript`/`HelmParamMappingRule`, cl. 6.3.4/6.3.5).
- **[MANO INTERPRETATION]** SOL018 cl. 7.4.2–7.4.6 profile `helm install`/`upgrade`/`rollback`/`uninstall`/`status` directly against the OS container workload management service interface requirements of IFA040 (`CismWkldMgt.00x`), i.e. Helm operations are how the CISM interface is realized for MCIOP-based lifecycle actions triggered by the VNFM.

So: **ETSI does not mention "Helm" as a VNFD keyword** (no `helm_chart` property exists anywhere in SOL001's non-Helm-artifact types) — support is expressed entirely through the **MCIOP** abstraction, which SOL018 then profiles onto Helm as one (currently the documented) concrete technology.

## 2. Can a VDU have BOTH an explicit OsContainer description AND a Helm/MCIOP association?

**Yes, as a normative/semantic inference — but see the correction below: this is weaker evidence than an explicit worked example.**

**[CORRECTION — 2026-09-04]** An earlier version of this document cited "SOL001 Annex A.22" as a worked VNFD example showing a single VDU with both an explicit `Vdu.OsContainer` **and** an `Mciop` association. That citation was re-checked against the SOL001 V5.4.1 table of contents and **is incorrect**: Annex A.22 is *"NSD with SAP to deployable modules mapping"* — an NS-level/SAP example, not a VNFD OsContainer+MCIOP example. No annex in the provided SOL001 V5.4.1 source shows both on the *same* VDU. The two relevant annexes that do exist are:

- **Annex A.18** — *"VNFD illustrating OsContainer modelling example"*: two `Vdu.OsContainerDeployableUnit` nodes (`vdu_1`, `vdu_2`), each with an explicit `container` requirement to `Vdu.OsContainer` node(s) — **no `Mciop` node appears anywhere in this annex.**
- **Annex A.23** — *"VNFD example with simplified design by using MCIOP"*: four `Vdu.OsContainerDeployableUnit` nodes (`Vdu1..Vdu4`) each associated to an `Mciop` (`Mciop1`/`Mciop2`) — **no `Vdu.OsContainer` node appears anywhere in this annex.**

So `CANNOT VERIFY EXACT CLAUSE FROM PROVIDED ETSI SOURCES` for a worked single-VDU-combined example specifically. The "both on one VDU" claim below now rests only on the two normative/semantic citations (IFA011 Note 10 + SOL003 grant text), which is retained as plausible but should not be read as "ETSI shows this exact pattern in a worked example."

| Evidence | Citation |
|---|---|
| Cardinality rule governing when each is *mandatory*, not a rule against combining them | IFA011 cl. 7.1.6.2.2, Note 10: *"In case the VDU ... is realized as OS containers, osContainerDesc **should** be present. In case the VDU ... is realized as OS containers and osContainerDesc is **not** present, the MciopProfile associated with the VDU **shall** be present in the VNFD."* — `osContainerDesc` is the recommended default; `MciopProfile` is a mandatory *fallback* only when it's missing. Nothing here forbids having both. |
| The NFVO's grant-processing logic **assumes** OsContainerDesc and MCIOP are normally used together | SOL003 V5.4.1 (Grant handling text, preceding cl. 9.5.3): *"Requests for resources to be allocated to MCIOs are derived from the OsContainerDesc resource templates referenced in the grant request ... An MCIOP profile contains a list of associated VDUs which in turn reference the OsContainerDesc resource templates. By using these associations, the NFVO can return the namespace for the resource definitions related to the MCIOP."* |
| Grant `ResourceDefinition`/`OsContainerDescData` handling explicitly anticipates the "OsContainerDesc absent, MCIOP present" case as the *exception*, not the rule | SOL003 cl. 9.5.3.2, Note 3: *"In case of the type of the ResourceDefinition is OSCONTAINER, either resourceTemplateId or osContainerDesc shall be present."* Note 4: *"In case the osContainerDesc is not present in the VNFD, while MCIOP(s) is present, osContainerDesc shall be included in the GrantVnfLifecycleOperationRequest based on the processing result of the MCIOP from CISM."* |

**Why you would want both in practice:** the `OsContainerDesc` (`Vdu.OsContainer` in TOSCA) is what the **NFVO uses for capacity planning and the Grant exchange** with the VNFM (cl. 9.5.3.2's `OSCONTAINER`-typed `ResourceDefinition`), independent of deployment technology. The **Helm chart/MCIOP** is what the **CISM actually executes** to realize the workload. Declaring both keeps NFVO-level admission control/capacity accounting accurate while still using Helm as the deployment mechanism — exactly the combination in `ExampleCorp_HybridWebCnf_vnf_pkg`.

## 3. TOSCA representation delivered (`ExampleCorp_HybridWebCnf_df_simple.yaml`)

| Node/artifact | Type (SOL001 V5.4.1 clause) | Role |
|---|---|---|
| `WebVdu` | `tosca.nodes.nfv.Vdu.OsContainerDeployableUnit` (6.8.13) | The VNFC/VDU construct |
| `WebVdu.requirements: [container: WebContainer]` | `container` requirement, capability `ContainerDeployable`, relationship `DeploysTo` (Table 6.8.13.4-1) | Links the VDU to its explicit OS-container resource description |
| `WebContainer` | `tosca.nodes.nfv.Vdu.OsContainer` (6.8.12) | Represents `OsContainerDesc` (IFA011 cl. 7.1.6.13) — cpu/memory requests+limits, image |
| `WebContainer.artifacts.sw_image` | `tosca.artifacts.nfv.SwImage` (6.3.1) | Represents `SwImageDesc` (IFA011 cl. 7.1.6.5) |
| `WebCp` | `tosca.nodes.nfv.VduCp` (6.8.5, referenced) | VDU connectivity |
| `web_mciop` | `tosca.nodes.nfv.Mciop` (6.8.14), `associatedVdu: WebVdu` | Represents `MciopProfile` (IFA011 cl. 7.1.8.20) — same `WebVdu` as above |
| `web_mciop.artifacts.web_helm_chart` | `tosca.artifacts.nfv.HelmChart` (6.3.3) | The actual Helm chart (`Artifacts/Charts/hybrid-web-cnf-1.0.0.tgz`), structured per SOL018 cl. 4.2.2.3.1 |
| `web_mciop.artifacts.web_param_mapping_script/rule` | `tosca.artifacts.nfv.HelmParamMappingScript`/`Rule` (6.3.4/6.3.5) | Generates the Helm `values.yaml` from the VNF LCM request |

**[ETSI NORMATIVE]** All constraints from the earlier `SimpleWebCnf` package still apply and are satisfied here too: max one `HelmChart`/`HelmParamMappingScript`/`HelmParamMappingRule` per `Mciop` (SOL001 cl. 6.8.14.7), and the script/rule pairing rule.

## 4. The parsed VNFD model — how an NFVO/VNFM turns this TOSCA into the IFA011 information model

`ExampleCorp_HybridWebCnf_vnf_pkg_PARSED_VNFD.json` (delivered alongside this document) is an **illustrative instance of the ETSI GS NFV-IFA 011 V5.4.1 `Vnfd` information element** (cl. 7.1.2) — i.e. what the TOSCA package resolves to once an NFVO/VNFM's descriptor parser has processed it. **This JSON shape is not itself an ETSI-defined file format** — IFA011 defines an information model, not a serialization — but every field name in it is a real IFA011 attribute, cited below:

| Parsed field | IFA011 information element / attribute | Derived from (TOSCA) |
|---|---|---|
| `vnfdId`, `vnfProvider`, `vnfProductName`, `vnfSoftwareVersion`, `vnfdVersion`, `vnfmInfo` | `Vnfd` top-level attributes (cl. 7.1.2.2) | `VNF` node template properties (`descriptor_id`, `provider`, `product_name`, `software_version`, `descriptor_version`, `vnfm_info`) |
| `vdu[0].vduId/name/description` | `Vdu` IE (cl. 7.1.6.2.2) | `WebVdu` node template name/`name`/`description` properties |
| `vdu[0].osContainerDesc` (reference) | `Vdu.osContainerDesc` attribute (cl. 7.1.6.2.2) | `WebVdu`'s `container` requirement targeting `WebContainer` |
| `osContainerDesc[0].*` | `OsContainerDesc` IE (cl. 7.1.6.13.2) | `WebContainer` node template properties (`requested_cpu_resources`, `cpu_resource_limit`, `requested_memory_resources`, `memory_resource_limit`) |
| `osContainerDesc[0].swImageDesc` (reference) | `OsContainerDesc.swImageDesc` attribute | `WebContainer.artifacts.sw_image` |
| `swImageDesc[0].*` | `SwImageDesc` IE (cl. 7.1.6.5.2) | `sw_image` artifact properties (`name`, `version`, `checksum`, `container_format` → `containerFormat`) and `file` (→ `swImage` reference) |
| `df[0].flavourId/description` | `VnfDf` IE (cl. 7.1.8.2.2) | `VNF.properties.flavour_id`/`flavour_description` |
| `df[0].vduProfile[0].*` | `VduProfile` IE (cl. 7.1.8.3.2) | `WebVdu.properties.vdu_profile` (`min/max_number_of_instances`) |
| `df[0].mciopProfile[0].mciopId/associatedVdu` | `MciopProfile` IE (cl. 7.1.8.20.2) | `web_mciop` node template name and its `associatedVdu` requirement |
| `df[0].mciopProfile[0].mciopParameterMappingRule/lcmOpParameterMappingScriptId` | `MciopProfile.mciopParameterMappingRule`/`lcmOpParameterMappingScriptId` | `web_mciop.artifacts.web_param_mapping_rule`/`web_param_mapping_script` |
| `vnfExtCpd[0]` | `VnfExtCpd` (cl. 7.1.6, referenced) | `WebCp` exposed via `substitution_mappings.virtual_link_mgmt` |

**[MANO INTERPRETATION] What happens with this parsed model at runtime (Helm path specifically):**

1. On `InstantiateVnf`, the VNFM builds a `GrantRequest` with a `ResourceDefinition` of `type: OSCONTAINER` referencing `osContainerDescId: OsContainerDesc-WebContainer` (SOL003 cl. 9.5.3.2/9.5.3.14) — this is the capacity/admission-control path, independent of Helm.
2. The NFVO resolves the MCIOP association (`mciopProfile[0].associatedVdu` → `WebVdu` → `osContainerDesc: OsContainerDesc-WebContainer`) to determine the container namespace for the grant response (SOL003 grant-handling text quoted in section 2 above).
3. Once granted, the VNFM invokes the `web_param_mapping_script` (per `lcmOpParameterMappingScriptId`) to render a Helm `values.yaml`, then the CISM's Helm-profiled interface executes `helm install {RELEASE} {CHART}` where `{CHART}` is the `web_helm_chart` artifact (SOL018 cl. 7.4.2) — this is the deployment path.
4. `Vdu.OsContainer`'s `cpu_resource_limit`/`memory_resource_limit` values are **not** automatically injected into the Kubernetes manifests generated by Helm — SOL018 Table 6.2.2.1-5 only maps them into `Container v1 core.resources.limits` for the **non-Helm, direct-Vdu.OsContainer-only** CISM path. In this **combined** design they exist for NFVO-side capacity governance; keeping the Helm chart's own `values.yaml`/`resources` block numerically consistent with them is a **[PROJECT-SPECIFIC]** integration responsibility, not something SOL001/SOL018 automate.

## 5. Compliance table

| Requirement | ETSI Spec | Clause | Normative level | Evidence | Status |
|---|---|---|---|---|---|
| `osContainerDesc` should be present for OS-container-realized VDUs | IFA011 | 7.1.6.2.2 Note 10 | SHOULD | `WebContainer` present via `container` requirement | PASS |
| `MciopProfile` mandatory only if `osContainerDesc` absent | IFA011 | 7.1.6.2.2 Note 10 | Conditional SHALL | Not triggered (`osContainerDesc` present) — `MciopProfile` included anyway, which is permitted | PASS |
| `VnfDf.mciopProfile` present when DF references MCIOP-based workloads | IFA011 | 7.1.8.2.2 | SHALL | `df[0].mciopProfile` populated | PASS |
| Max one `HelmChart`/`HelmParamMappingScript`/`HelmParamMappingRule` per `Mciop` | SOL001 | 6.8.14.7 | SHALL | One of each on `web_mciop` | PASS |
| `HelmParamMappingRule` only with `HelmParamMappingScript` | SOL001 | 6.8.14.7 | SHALL | Both present together | PASS |
| Grant `ResourceDefinition` for OSCONTAINER carries `resourceTemplateId` or `osContainerDesc` | SOL003 | 9.5.3.2 Note 3 | SHALL | Applicable at runtime via `osContainerDesc` reference; not asserted by the static VNFD itself | NOT SPECIFIED (runtime behavior, outside VNFD scope) |
| Full CSAR/manifest structure conformance (SOL004) | SOL004 | — | — | Not among provided ETSI sources | CANNOT VERIFY |

## 6. Self-audit

- Verified the MCIOP/Helm relationship is `MCIOP = ETSI concept, Helm chart = SOL018-profiled implementation of it`, not "ETSI defines Helm directly." ✔
- **Correction applied 2026-09-04:** the original self-audit here wrongly claimed a real SOL001 worked example (Annex A.22) backed the "single VDU with both OsContainer + Mciop" claim. Re-checked against the SOL001 V5.4.1 table of contents: Annex A.22 is actually an NSD/SAP-to-deployable-modules example, not a VNFD OsContainer+MCIOP example. The claim now correctly rests only on IFA011 Note 10 + SOL003 grant-handling prose (inference, not a worked example); the two real worked examples found (A.18: OsContainer-only, A.23: MCIOP-only) are cited as separate, non-combined patterns — see companion document `ETSI_TOSCA_to_VNFD_Parsing_Design.md` for a design that uses these two real patterns on two separate VDUs instead. ✔
- Did not present the parsed-VNFD JSON shape itself as an ETSI-defined format — flagged it as illustrative, with every field traced to a real IFA011 attribute and clause. ✔
- Labeled the Helm `values.yaml`/`Vdu.OsContainer` resource-limit consistency as `[PROJECT-SPECIFIC]`, since SOL001/SOL018 do not automate that reconciliation for the combined design. ✔
- SOL004 (VNF package/CSAR/manifest format) remains outside the provided source set — flagged again where relevant. ✔
