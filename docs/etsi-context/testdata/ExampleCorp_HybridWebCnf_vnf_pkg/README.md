# ExampleCorp HybridWebCnf - VNF package (combined OsContainer + Helm/MCIOP)

Unlike `ExampleCorp_SimpleWebCnf_vnf_pkg` (which used SOL001 Annex A.23's
"simplified design" - Mciop/Helm chart only, no `Vdu.OsContainer`), this
package uses the **"regular" design**: the `WebVdu`
(`tosca.nodes.nfv.Vdu.OsContainerDeployableUnit`) declares an explicit
`WebContainer` (`tosca.nodes.nfv.Vdu.OsContainer`) child describing its
CPU/memory/image resource requirements, **and** is also associated with an
`Mciop` node (`web_mciop`) carrying the Helm chart that the CISM actually
uses to deploy it. See `ETSI_VNFD_Helm_Support_and_Parsing.md` (delivered
alongside this package) for why ETSI NFV V5.4.1 supports this combination
and how the resulting VNFD is expected to be parsed by an NFVO/VNFM.

## Before on-boarding into a real NFVO/VNFM

1. Add `Definitions/etsi_nfv_sol001_vnfd_types.yaml` from the official ETSI
   location (SOL001 V5.4.1 Annex B.2) - see the sibling package's README
   for the exact URL.
2. Replace `Artifacts/Images/README.txt` with your real container image
   artifact/reference, and update the `sw_image` artifact's `checksum`,
   `size`, and `version` properties in
   `Definitions/ExampleCorp_HybridWebCnf_df_simple.yaml` to match.
3. Replace `Artifacts/Charts/hybrid-web-cnf*` with your real Helm chart.
4. As with the other package, the full CSAR/manifest structure (SOL004) is
   outside the ETSI sources available for this design and is therefore not
   asserted here (`CANNOT VERIFY FROM PROVIDED ETSI SOURCES`).

## Layout

```
TOSCA-Metadata/TOSCA.meta
Definitions/
  ExampleCorp_HybridWebCnf_df_simple.yaml   Main VNFD (VNF type + topology)
  etsi_nfv_sol001_vnfd_types.yaml            <- add from ETSI forge
Artifacts/
  Images/README.txt                          <- replace with real OS container image ref (SwImage artifact)
  Charts/hybrid-web-cnf/                      Helm chart source
  Charts/hybrid-web-cnf-1.0.0.tgz             Packaged chart (tosca.artifacts.nfv.HelmChart)
  Scripts/lcm_param_mapping.sh                tosca.artifacts.nfv.HelmParamMappingScript
  Scripts/lcm_param_mapping_rules.txt         tosca.artifacts.nfv.HelmParamMappingRule
```
