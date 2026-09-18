# ExampleCorp SimpleWebCnf - VNF package (before on-boarding)

This package is a TOSCA/VNFD example built against **ETSI NFV Release 5,
V5.4.1** (SOL001 / SOL018 / IFA011), demonstrating an ETSI-compliant CNF
realized entirely via a Helm chart (MCIOP), with the corresponding VNFD
mapping. See `ETSI_VNFD_Helm_Mapping.md` (delivered alongside this package)
for the full ETSI-grounded design rationale, traceability matrix and
compliance table.

## Before on-boarding into a real NFVO/VNFM

1. **Add the ETSI NFV TOSCA type definitions file.** This package's
   `Definitions/ExampleCorp_SimpleWebCnf_df_simple.yaml` imports
   `etsi_nfv_sol001_vnfd_types.yaml`, which this package does **not**
   include (it is a large, ETSI-maintained file and must not be
   hand-authored or fabricated). Download it from the official location
   cited in ETSI GS NFV-SOL 001 V5.4.1 Annex B.2:
   `https://forge.etsi.org/rep/nfv/SOL001/raw/v5.4.1/etsi_nfv_sol001_vnfd_types.yaml`
   and place it at `Definitions/etsi_nfv_sol001_vnfd_types.yaml`. Per
   Annex B.2 Note 1, that file itself imports
   `etsi_nfv_sol001_common_types.yaml`, which must be added the same way
   if your VNFD parser requires it as a local file rather than resolving
   the import remotely.
2. **Replace the example artifacts** (`Artifacts/Charts/simple-web-cnf*`,
   `Artifacts/Scripts/lcm_param_mapping*`) with your own container image,
   Helm chart, and (if you use one) LCM parameter-mapping script/rule.
3. **Add a VNF package manifest and, if required, signing/security
   artifacts.** The manifest (`.mf`) file format and the full CSAR package
   structure/naming rules are defined in ETSI GS NFV-SOL 004, which is
   **not** among the ETSI specifications available for this task
   (`CANNOT VERIFY FROM PROVIDED ETSI SOURCES`) - only the `Entry-Definitions`
   / `Other-Definitions` `TOSCA.meta` keywords and the
   `TOSCA-Metadata/`, `Definitions/`, `Artifacts/` directory roles could be
   verified, via ETSI GS NFV-SOL 003 V5.4.1 clause 10.4.4.3.2 and ETSI
   GS NFV-SOL 005 V5.4.1 clause 9.4.4.3.2. Consult SOL004 directly for the
   manifest and any additional mandatory package-level files.

## Layout

```
TOSCA-Metadata/TOSCA.meta            Entry-Definitions / Other-Definitions pointers
Definitions/
  ExampleCorp_SimpleWebCnf_df_simple.yaml   Main VNFD (VNF type + topology)
  etsi_nfv_sol001_vnfd_types.yaml            <- add from ETSI forge, see above
Artifacts/
  Charts/simple-web-cnf/              Helm chart source
  Charts/simple-web-cnf-1.0.0.tgz     Packaged chart (tosca.artifacts.nfv.HelmChart)
  Scripts/lcm_param_mapping.sh        tosca.artifacts.nfv.HelmParamMappingScript
  Scripts/lcm_param_mapping_rules.txt tosca.artifacts.nfv.HelmParamMappingRule
```
