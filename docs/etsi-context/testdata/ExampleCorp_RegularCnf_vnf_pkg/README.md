# ExampleCorp RegularCnf - VNF package (explicit OsContainer, no MCIOP/Helm)

This is the **"regular" / standard containerized VNFD design**: every VDU declares an
explicit `tosca.nodes.nfv.Vdu.OsContainer` child node describing its OS container
resource capacity (CPU/memory/image). **No `tosca.nodes.nfv.Mciop` node and no Helm
artifact appear anywhere in this package** - a CISM realizes each VDU directly from its
`OsContainerDesc`, without a Helm chart. This is the baseline "normal" CNF pattern,
structured after **SOL001 V5.4.1 Annex A.18 ("VNFD illustrating OsContainer modelling
example")** - the only ETSI worked example of a purely OsContainer-based (no MCIOP)
VNFD found in the provided source set.

## What's inside

Two VDUs, one deployment flavour (`simple`):

- **Vdu1** ("frontend-vdu") - one `Vdu.OsContainer` child (`Vdu1Container`) with a
  `SwImage` artifact. Its `VduCp` (`Vdu1Cp`) is re-exposed as the VNF's external
  connection point via `substitution_mappings` (SOL001 clause 6.8.2.8 / Annex A.3.2
  pattern) - no explicit `VnfExtCp` node template is needed for this.
- **Vdu2** ("backend-vdu") - one `Vdu.OsContainer` child (`Vdu2Container`) plus an
  attached `Vdu.VirtualBlockStorage` (`Vdu2Storage`, via the `virtual_storage`
  requirement, SOL001 Table 6.8.13.4-1) - demonstrating the storage-attached VDU
  pattern that Annex A.18's `vdu_2` also illustrates.
- **InternalVl** (`tosca.nodes.nfv.VnfVirtualLink`, clause 6.8.9) connects `Vdu1` and
  `Vdu2` internally via `Vdu1InternalCp`/`Vdu2Cp`.

## Deliberate simplifications vs. the literal Annex A.18 example (noted for your review)

- Annex A.18 exposes `vdu_1`'s service via a `tosca.nodes.nfv.VirtualCp` (a VIP-style
  CP that can front multiple VDU instances). This package instead re-exposes `Vdu1Cp`
  directly as the external CP - simpler, and sufficient for a "regular" single-instance
  VDU design. Happy to add the `VirtualCp` variant if you need that HA pattern.
- Annex A.18's `vdu_2` has multiple containers (2+); this package gives each VDU
  exactly one `Vdu.OsContainer` child, to keep the first review pass simple. Adding a
  second co-located container to a VDU only requires a second `container` requirement
  occurrence on the same `Vdu.OsContainerDeployableUnit` node plus another
  `Vdu.OsContainer` node.

## Before on-boarding into a real NFVO/VNFM

1. Add `Definitions/etsi_nfv_sol001_vnfd_types.yaml` from the official ETSI location
   (SOL001 V5.4.1 Annex B.2):
   `https://forge.etsi.org/rep/nfv/SOL001/raw/v5.4.1/etsi_nfv_sol001_vnfd_types.yaml`
2. Replace `Artifacts/Images/{frontend,backend}/README.txt` with real container image
   artifacts/references, and update the corresponding `sw_image` artifact's
   `checksum`/`size`/`version` properties in the VNFD to match.
3. The full CSAR/manifest structure (SOL004) is outside the ETSI sources available for
   this design and is therefore not asserted here (`CANNOT VERIFY FROM PROVIDED ETSI
   SOURCES`).

## Layout

```
TOSCA-Metadata/TOSCA.meta
Definitions/
  ExampleCorp_RegularCnf_df_simple.yaml   Main VNFD (VNF type + topology)
  etsi_nfv_sol001_vnfd_types.yaml          <- add from ETSI forge
Artifacts/
  Images/frontend/README.txt               <- replace with real image ref (Vdu1Container's SwImage)
  Images/backend/README.txt                <- replace with real image ref (Vdu2Container's SwImage)
```
