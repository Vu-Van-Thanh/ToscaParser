# ExampleCorp_MultiDfCnf_vnf_pkg

The **two-level service template design**, SOL001 V5.4.1 clause 6.11.2. Until this package existed
that whole branch of the parser had no fixture touching it.

`[ETSI NORMATIVE]` Clause 6.11.2: the VNFD "shall be implemented as one top-level service template
and one or multiple lower level service templates, where each lower level service template
represents a deployment flavour". The lower level templates shall be named in `Other-Definitions`.

## Layout

```
TOSCA-Metadata/TOSCA.meta
  Entry-Definitions:  Definitions/ExampleCorp_MultiDfCnf_top.yaml
  Other-Definitions:  ..._df_simple.yaml, ..._df_complex.yaml, etsi_nfv_sol001_vnfd_types.yaml
Definitions/
  ExampleCorp_MultiDfCnf_vnf_node_type.yaml   the VNF node type all three templates share
  ExampleCorp_MultiDfCnf_top.yaml             top level, one abstract VNF node, directives: [substitute]
  ExampleCorp_MultiDfCnf_df_simple.yaml       flavour "simple"  - 1 VDU
  ExampleCorp_MultiDfCnf_df_complex.yaml      flavour "complex" - 2 VDUs, ONE Mciop for both
```

Flavour selection uses `substitution_filter`:

```yaml
substitution_mappings:
  node_type: ExampleCorp.MultiDfCnf.1_0
  substitution_filter:
    properties:
      - flavour_id: { equal: complex }
```

`[ETSI NORMATIVE]` Clause 6.11.2 NOTE 1 records that the older `property_mapping` grammar was
replaced from v3.3.1 onwards, so `substitution_filter` is the current form.

## The assertion that matters

`complex_mciop` declares the **same requirement key twice**:

```yaml
requirements:
  - associatedVdu: FrontVdu
  - associatedVdu: WorkerVdu
```

`[ETSI NORMATIVE]` SOL001 clause 6.8.14.6 gives `associatedVdu` occurrences `[1, UNBOUNDED]`, and
TOSCA writes requirements as a sequence of single-entry maps precisely so a key may repeat. A parser
that reads that sequence into a `Map<String,String>` keeps only the last entry and silently loses
`WorkerVdu` — with no error anywhere. SOL001 Annex A.23 relies on the same shape.

## Expected parse

```
df                 2      flavourId: simple, complex
df[simple]         vduProfile [FrontVdu]              mciopProfile [(simple_mciop,  [FrontVdu])]
df[complex]        vduProfile [FrontVdu, WorkerVdu]   mciopProfile [(complex_mciop, [FrontVdu, WorkerVdu])]
vdu                FrontVdu -> MCIOP_CISM,  WorkerVdu -> MCIOP_CISM
mciopId            [simple_mciop, complex_mciop]
findings           INFO YAML02 (two-level design detected) - and nothing else
```

The top-level template is **not** a flavour: it is the `Entry-Definitions` file, and what
distinguishes it is that other files in the package also carry topologies.

This package also ships a `.mf` manifest and a `ChangeLog.txt`, and writes
`vnfm_info: ['0:ExampleCorp-1.0.0']`, so unlike the three original fixtures neither C13 nor TOSCA03
fires. It is the clean control against which their warnings can be read.
