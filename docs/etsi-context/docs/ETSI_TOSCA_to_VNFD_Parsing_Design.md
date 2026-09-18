# Design idea: hybrid TOSCA VNFD (2 VDU) + general TOSCA → VNFD parsing rule

**Status:** concept only — no parser code written yet (per explicit request). This document is the
grounding reference for the future `etsi-vnfd-parser` library.

**Scope:** CNF (OS-container-realized VDU) flow only, per explicit instruction — VM-based VNF
(`Vdu.Compute`/`VirtualComputeDesc`) flow is out of scope and intentionally not covered below.

**Target standard:** ETSI NFV Release 5, V5.4.1 (SOL001, IFA011 primary; SOL003/SOL018 for
MANO/CISM interpretation). IFA040 used only where noted, flagged `[VERSION MISMATCH] IFA040 V5.2.1`
(uploaded version; no V5.4.1 release exists yet, per user's 2026-09-04 instruction to use it as a
temporary working document).

---

## 0. Correction carried over from `ETSI_VNFD_Helm_Support_and_Parsing.md`

That document previously cited "SOL001 Annex A.22" as a worked example of a single VDU with both
`Vdu.OsContainer` and `Mciop`. Re-checked against the SOL001 V5.4.1 table of contents: **Annex A.22
is "NSD with SAP to deployable modules mapping"** (NS-level), not a VNFD example. The citation has
been corrected in that document. The two annexes that actually exist and matter here are:

| Annex | Title | Pattern |
|---|---|---|
| **A.18** | "VNFD illustrating OsContainer modelling example" | Two `Vdu.OsContainerDeployableUnit` nodes (`vdu_1`, `vdu_2`), each with an explicit `container` requirement to a `Vdu.OsContainer` node. **No `Mciop` node in this annex.** |
| **A.23** | "VNFD example with simplified design by using MCIOP" | Four `Vdu.OsContainerDeployableUnit` nodes (`Vdu1..Vdu4`), each associated to an `Mciop` node via `associatedVdu`. **No `Vdu.OsContainer` node in this annex.** |

This is the basis for the corrected "hybrid" design below: instead of forcing both patterns onto one
VDU (which is not backed by a worked ETSI example, only by inference from IFA011 Note 10 + SOL003
grant text), **apply each of ETSI's own real patterns to a separate VDU inside the same VNF/DF.**
This is exactly the user's original hybrid intent: *"1 package tosca file có cả khai vdu theo helm
chart, có cả khai vdu đủ thông tin os_container — tức là vnf này có thể có 2 cái vdu."*

---

## 1. Idea A — Hybrid VNFD with two separate VDUs

### 1.1 Concept

One VNF, one deployment flavour (`simple`), two `Vdu.OsContainerDeployableUnit` node templates:

```
VNF: ExampleCorp.HybridWebCnf2.1_0
 ├─ CacheVdu   (Vdu.OsContainerDeployableUnit)  — MCIOP-only pattern, per Annex A.23
 │    associatedVdu ← cache_mciop (Mciop)
 │                        ├─ HelmChart artifact          (Redis-style cache chart)
 │                        ├─ HelmParamMappingScript
 │                        └─ HelmParamMappingRule
 │    (NO "container" requirement — no Vdu.OsContainer node for this VDU)
 │
 └─ WebVdu     (Vdu.OsContainerDeployableUnit)  — explicit-OsContainer pattern, per Annex A.18
      requirements: [ container: WebContainer ]
                        └─ WebContainer (Vdu.OsContainer)
                             └─ sw_image artifact (SwImage)
      (NO Mciop association — CISM/VNFM realizes this VDU directly from OsContainerDesc,
       not via a Helm chart)
```

Both VDUs sit in the same `topology_template.node_templates` / same DF, connected to the VNF's
external connectivity via their own `VduCp` nodes as needed.

### 1.2 Why this is a cleaner "hybrid" than the earlier single-VDU design

- **[ETSI SEMANTIC]** IFA011 cl. 7.1.6.2.2 Note 10 applies *per VDU*, independently: "In case the VDU
  ... is realized as OS containers, osContainerDesc should be present. In case ... osContainerDesc is
  not present, the MciopProfile associated with the VDU shall be present." `CacheVdu` takes the
  "MciopProfile present, osContainerDesc absent" branch; `WebVdu` takes the "osContainerDesc present"
  branch. Nothing requires the same choice for every VDU in a DF.
- **[ETSI NORMATIVE]** IFA011 cl. 7.1.8.2.2: `VnfDf.vduProfile` cardinality is **1..N** (a DF
  references many VDUs) and `VnfDf.mciopProfile` cardinality is **0..N**, present "if the DF
  references (via the vduProfile) containerized workloads based on a MCIOP" — i.e. the MCIOP
  attribute is scoped to whichever VDUs actually use it, not an all-or-nothing switch for the DF.
- Each VDU individually matches a **real SOL001 worked example** (A.18 for `WebVdu`, A.23 for
  `CacheVdu`), rather than needing an example that doesn't exist (a single VDU with both).

### 1.3 Illustrative TOSCA sketch (concept, not the final file)

```yaml
node_templates:
  VNF:
    type: ExampleCorp.HybridWebCnf2.1_0
    properties: { flavour_id: simple, ... }

  # --- Pattern per Annex A.23: MCIOP-only VDU ---
  CacheVdu:
    type: tosca.nodes.nfv.Vdu.OsContainerDeployableUnit
    properties:
      name: cache-vdu
      description: "Cache tier, deployed and fully described by its Helm chart"
      vdu_profile: { min_number_of_instances: 1, max_number_of_instances: 1 }
      mcio_identification_data: { name: cache, type: Deployment }
      is_num_of_instances_cluster_based: false
    # NOTE: no "container" requirement -> osContainerDesc will be ABSENT for this VDU

  cache_mciop:
    type: tosca.nodes.nfv.Mciop
    requirements:
      - associatedVdu: CacheVdu
    artifacts:
      cache_helm_chart: { type: tosca.artifacts.nfv.HelmChart, file: ../Artifacts/Charts/cache-1.0.0.tgz }
      cache_param_mapping_script: { type: tosca.artifacts.nfv.HelmParamMappingScript, file: ..., properties: { language: bash } }
      cache_param_mapping_rule: { type: tosca.artifacts.nfv.HelmParamMappingRule, file: ... }

  # --- Pattern per Annex A.18: explicit OsContainer VDU, no MCIOP ---
  WebVdu:
    type: tosca.nodes.nfv.Vdu.OsContainerDeployableUnit
    properties:
      name: web-vdu
      description: "Web tier, described directly via OsContainerDesc (no Helm chart)"
      vdu_profile: { min_number_of_instances: 1, max_number_of_instances: 5 }
      mcio_identification_data: { name: web, type: Deployment }
      is_num_of_instances_cluster_based: false
    requirements:
      - container: WebContainer

  WebContainer:
    type: tosca.nodes.nfv.Vdu.OsContainer
    properties:
      name: web-container
      requested_cpu_resources: 250
      cpu_resource_limit: 500
      requested_memory_resources: 128 MiB
      memory_resource_limit: 256 MiB
    artifacts:
      sw_image: { type: tosca.artifacts.nfv.SwImage, file: ../Artifacts/Images/web.tar, properties: { ... } }
```

`[ASSUMPTION]` For `WebVdu` (no MCIOP), how the CISM actually realizes the OS container
(what management/orchestration interface it calls) is IFA040's territory
(`[VERSION MISMATCH] IFA040 V5.2.1`) — not fully re-verified yet against that document for this
design; flagged for follow-up rather than asserted.

### 1.4 Parsed-VNFD shape for this hybrid (concept)

```
Vnfd
 ├─ vdu[0] = { vduId: CacheVdu, osContainerDesc: [] }              # empty — Note 10 "absent" branch
 ├─ vdu[1] = { vduId: WebVdu,   osContainerDesc: [OsContainerDesc-WebContainer] }
 ├─ osContainerDesc[0] = { osContainerDescId: OsContainerDesc-WebContainer, ... }
 ├─ swImageDesc[0]     = { id: SwImageDesc-WebImage, ... }
 └─ df[0]
     ├─ vduProfile: [ {vduId: CacheVdu, min:1,max:1}, {vduId: WebVdu, min:1,max:5} ]
     └─ mciopProfile: [ {mciopId: cache_mciop, associatedVdu:[CacheVdu], ...} ]
                         # only ONE mciopProfile entry — WebVdu is not referenced by any MciopProfile
```

This is the key structural signal a parser must produce correctly: **`mciopProfile[].associatedVdu`
covers a subset of `vduProfile[].vduId`, not all of them.**

---

## 2. Idea B — General TOSCA package → IFA011 VNFD parsing rule (for any package, not just this example)

This is the rule set the future library encodes. No code here — just the element-by-element mapping
and the algorithm shape.

### 2.1 Package-level (informative — SOL004 not in the provided source set)

`CANNOT VERIFY FROM PROVIDED ETSI SOURCES` for the exact CSAR/manifest structure (SOL004 not
uploaded; V5.4.1 doesn't exist yet per earlier discussion). Practical, convention-based steps only:

1. Read `TOSCA-Metadata/TOSCA.meta` → `Entry-Definitions` names the main service template.
2. Load that YAML, follow its `imports:` list, merge `data_types` / `node_types` / `artifact_types`
   across all imported files that are physically present in the package.
3. The official `etsi_nfv_sol001_vnfd_types.yaml` (SOL001 Annex B.2) defines the ETSI base types
   (`tosca.nodes.nfv.VNF`, `Vdu.OsContainer`, `Mciop`, ...) but is often *referenced*, not *shipped*,
   in a package (see both example packages' READMEs). A parser therefore needs a built-in fallback
   catalogue of these base type names/clauses so `derived_from` chains can still be resolved to an
   ETSI "kind" even when the external file isn't present in the CSAR being parsed.

### 2.2 Node-level mapping table (SOL001 → IFA011) — CNF rows only

**[ETSI NORMATIVE — primary source]** This table is not a self-derived mapping. SOL001 V5.4.1 defines
the mapping officially in two places, and the rows below are restricted to those two tables' entries
that are relevant to the **CNF / OS-container-realized VDU flow** (VM-based rows from the same tables
are deliberately omitted per current scope):

- **Clause 6.1, Table 6.1-1** — "Mapping of ETSI GS NFV-IFA 011 [1] information elements with TOSCA
  types" (coarse, information-element-level).
- **Annex A.9.2, Table A.9.2-1** — "Mapping between ETSI GS NFV-IFA 011 [1] IM and TOSCA concepts"
  (finer-grained, per-clause, also covers interfaces/policies/properties, not only nodes).

| IFA011 element (clause) | TOSCA concept (per Table 6.1-1 / A.9.2-1) | Notes from the ETSI tables themselves |
|---|---|---|
| VNFD (7.1.2) | `tosca.nodes.nfv.VNF`, derived from `tosca.nodes.Root` | Whole service template(s) in the VNF package represent the VNFD |
| **Vdu** (7.1.6) | **n/a** — represented by a *collection* of: `tosca.nodes.nfv.VduCp`, `tosca.nodes.nfv.Vdu.OsContainerDeployableUnit`, `tosca.nodes.nfv.Vdu.OsContainer` (CNF path only — VM path omitted here) | Table 6.1-1 Note 1: Vdu is **not** one node type |
| VduCpd (7.1.6.4) | `tosca.nodes.nfv.VduCp` | |
| SwImageDesc (7.1.6.5) | `tosca.artifacts.nfv.SwImage`, derived from `tosca.artifacts.Deployment.Image` | artifact, not a node |
| **OsContainerDesc** | **`tosca.nodes.nfv.Vdu.OsContainer`**, derived from `tosca.nodes.Root` | direct mapping — matches the design already used |
| VnfExtCpd (7.1.4) | `tosca.nodes.nfv.VnfExtCp` **or** `tosca.nodes.nfv.VduCp` **or** `tosca.nodes.nfv.Cp` | depends on how the CP is exposed (see 2.2.1 below) |
| **VnfDf** | **n/a** | Table 6.1-1 Note 2: an entire TOSCA service template represents one VnfDf, not a node |
| `tosca.nodes.nfv.Mciop` ↔ **MciopProfile** | **no direct mapping** (Table 6.1-1 Note 3) | Only `deploymentOrder` and `associatedVdu` attributes of `MciopProfile` map onto the `Mciop` node. `affinityOrAntiAffinityGroupId` maps instead to a **separate TOSCA policy** — `tosca.policies.nfv.AffinityRule` or `AntiAffinityRule` — not to any property of the `Mciop` node itself. **[correction to this document's earlier version, which treated `MciopProfile` as mapping onto `Mciop` as a whole]** |
| VnfLcmOperationsConfiguration (7.1.5) | property of `VNF` node type + `additional_parameters` inputs on the `Vnflcm` interface | |
| LifeCycleManagementScript (7.1.13) | interface `tosca.interfaces.nfv.Vnflcm` or `ChangeCurrentVnfPackage` | |

#### 2.2.1 Element-level parsing rule (elaborating the official table into an algorithm)

| TOSCA node type (SOL001 clause) | Recognized by (derived_from walk) | IFA011 target | Key property/requirement mapping |
|---|---|---|---|
| `tosca.nodes.nfv.VNF` (6.8.1) | node template's type ultimately derives from this | `Vnfd` top-level (cl. 7.1.2.2) | `descriptor_id`→`vnfdId`, `provider`→`vnfProvider`, `product_name`→`vnfProductName`, `software_version`→`vnfSoftwareVersion`, `descriptor_version`→`vnfdVersion`, `vnfm_info`→`vnfmInfo`, `flavour_id`/`flavour_description`→`VnfDf.flavourId`/`description` (7.1.8.2.2) |
| `tosca.nodes.nfv.Vdu.OsContainerDeployableUnit` (6.8.13) | derived_from walk | `Vdu` IE (cl. 7.1.6.2.2) | node template name→`vduId`, `name`/`description` properties direct, `vdu_profile`→`VduProfile` IE (7.1.8.3.2) under the DF |
| — same node's `container` requirement (Table 6.8.13.4-1), if present | requirement target | `Vdu.osContainerDesc` reference | resolves to the `Vdu.OsContainer` node named as the requirement's target |
| — same node's absence of `container` requirement | — | `Vdu.osContainerDesc` = empty | parser must then confirm some `Mciop.associatedVdu` in the DF references this `vduId` (IFA011 Note 10) — if neither is found, flag `WARNING`/`FAIL`, not silently pass |
| `tosca.nodes.nfv.Vdu.OsContainer` (6.8.12) | derived_from walk, reached via a `container` requirement | `OsContainerDesc` IE (cl. 7.1.6.13.2) | `requested_cpu_resources`→`requestedCpuResources`, `cpu_resource_limit`→`cpuResourceLimit`, `requested_memory_resources`→`requestedMemoryResources`, `memory_resource_limit`→`memoryResourceLimit` |
| — its `sw_image` artifact, type `tosca.artifacts.nfv.SwImage` (6.3.1) — required, max 1 (cl. 6.8.12.6) | artifact type match | `SwImageDesc` IE (cl. 7.1.6.5.2), referenced from `OsContainerDesc.swImageDesc` | `name`, `version`, `checksum`, `container_format`→`containerFormat`, artifact `file`→`swImage` |
| `tosca.nodes.nfv.Mciop` (6.8.14) | derived_from walk | `MciopProfile` IE (cl. 7.1.8.20.2) under the DF — **partial mapping only**, see 2.2 table above | node template name→`mciopId` (by convention, not an explicit ETSI-stated rule — flagged `[ASSUMPTION]`), `associatedVdu` requirement→`associatedVdu` list, `deploymentOrder` property (if modeled) → `deploymentOrder` |
| — its `HelmChart`/`HelmParamMappingScript`/`HelmParamMappingRule` artifacts (6.3.3/6.3.4/6.3.5), max one each (cl. 6.8.14.7), script+rule must pair | artifact type match | `MciopProfile.lcmOpParameterMappingScriptId` / `.mciopParameterMappingRule`, and an `LcmOpParameterMappingScript` IE (cl. 7.1.20) | direct field copy + cross-reference |
| any `tosca.policies.nfv.AffinityRule`/`AntiAffinityRule` targeting an `Mciop` node | policy `targets` list | `MciopProfile.affinityOrAntiAffinityGroupId` | **new, from Table 6.1-1 Note 3** — this attribute is NOT read off the `Mciop` node itself; the parser must separately scan `topology_template.policies` for entries targeting the Mciop node template |
| `tosca.nodes.nfv.VduCp` (6.8.5) | derived_from walk | `VduCpd`, listed in owning `Vdu.intCpd` (cl. 7.1.6.4/7.1.6.2.2) | `virtual_binding` requirement identifies the owning VDU |
| — same `VduCp`, if also named in `topology_template.substitution_mappings.requirements` | cross-reference against substitution_mappings, not visible on the node alone | also becomes a `VnfExtCpd` | this is a two-pass step: node-level parse first, then a substitution_mappings pass to reclassify exposed CPs |

### 2.3 DF/flavour-level assembly

1. One service template file == one deployment flavour, per SOL001 cl. 6.11.3 — each parsed file
   becomes one `VnfDf` entry (`flavourId`/`description` from the `VNF` node template's properties).
2. `VnfDf.vduProfile` = one entry per `Vdu.OsContainerDeployableUnit` node template found in that
   file's topology (CNF scope only — a `Vdu.Compute` node would also produce a `VduProfile` entry per
   IFA011, but that VM-based path is out of scope here).
3. `VnfDf.mciopProfile` = one entry per `Mciop` node template found — **only if at least one exists**;
   IFA011 cl. 7.1.8.2.2 makes this conditional, not unconditional.
4. `instantiationLevel`: if the package doesn't model `tosca.policies.nfv.VduInstantiationLevels` /
   `InstantiationLevels` policies explicitly, the parser synthesizes a single `default` level — per
   IFA011 cl. 7.1.8.2.2 ("If there is only one instantiationLevel entry, it shall be treated as the
   default"). This synthesis must be labeled `[MANO INTERPRETATION]`/`[ASSUMPTION]` in the parser's
   output, not presented as literally present in the source VNFD.

### 2.4 Compliance checks a parser should run while building the model (not just transcribe)

| Check | Rule | Clause |
|---|---|---|
| Every `Vdu.OsContainerDeployableUnit` has osContainerDesc **or** is covered by some `MciopProfile.associatedVdu` | Note 10 fallback rule | IFA011 7.1.6.2.2 |
| `Mciop` node has at most one `HelmChart`, one `HelmParamMappingScript`, one `HelmParamMappingRule` | cardinality | SOL001 6.8.14.7 |
| `HelmParamMappingRule` present ⇒ `HelmParamMappingScript` also present | pairing rule | IFA011 7.1.8.20.2 Note 3 |
| `Vdu.OsContainer` has exactly one `SwImage` artifact | cardinality | SOL001 6.8.12.6 |
| An `Mciop`'s `associatedVdu` list doesn't mix VDUs from different deployable modules | deployable-module consistency | IFA011 Annex A.5 |

### 2.5 Open questions / assumptions for the future library

- `[ASSUMPTION]` The type registry's fallback catalogue (2.1.3) needs to be built once from SOL001
  Annex B section + the clause tables already verified in this project's prior work, kept separate
  from the CSAR-parsing logic so it can be updated if SOL001 changes.
- `[ASSUMPTION]` Multi-flavour packages (more than one service template) are architecturally handled
  by looping section 2.3 per file — not yet tested against a real multi-DF package.
- `CANNOT VERIFY FROM PROVIDED ETSI SOURCES`: exact SOL004 CSAR packaging/manifest rules. The parser
  will need a documented, explicitly-labeled convention (not an ETSI citation) for locating
  `TOSCA.meta` and resolving relative artifact paths.
- IFA040 (CISM information model) has not yet been consulted for **how** a non-MCIOP `Vdu.OsContainer`
  VDU (like `WebVdu` in section 1) is actually realized by the CISM at runtime — flagged as a follow-up
  read, not asserted here, and any citation from it will carry `[VERSION MISMATCH] IFA040 V5.2.1`.

## 3. Self-audit

- [x] Corrected the earlier Annex A.22 mis-citation (checked the actual SOL001 V5.4.1 TOC) before
      building on it further.
- [x] Hybrid design now maps each VDU to a real worked SOL001 example (A.18, A.23) individually,
      rather than asserting an unverified combined example.
- [x] Did not invent any new SOL001/IFA011 clause numbers; reused only clauses already verified in
      this project's prior searches, or newly quoted directly above.
- [x] Kept the VM-based (`Vdu.Compute`/`VirtualComputeDesc`) path entirely out of scope, per explicit
      user instruction — removed from the mapping tables rather than left as a partial/guessed row.
- [x] Replaced the self-derived node↔IFA011 mapping table with SOL001's own official tables
      (clause 6.1 Table 6.1-1, Annex A.9.2 Table A.9.2-1) as the primary source, restricted to the
      CNF-relevant rows, and corrected the `MciopProfile`↔`Mciop` mapping per Table 6.1-1 Note 3
      (affinity/anti-affinity comes from a separate TOSCA policy, not the `Mciop` node).
- [x] No code produced — this is architecture/mapping-rule design only, per explicit instruction.
