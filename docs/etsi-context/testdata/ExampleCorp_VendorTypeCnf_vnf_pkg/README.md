# ExampleCorp_VendorTypeCnf_vnf_pkg

Nothing in this topology names an ETSI type. Every node template and the one artifact declare a
vendor type, and three of them sit **two levels** below the ETSI type they derive from.

`[ETSI NORMATIVE]` SOL001 V5.4.1 clause 6.11.2 requires the VNFD to declare a node type specific to
the VNF and derived from `tosca.nodes.nfv.VNF`. A consumer therefore never sees
`tosca.nodes.nfv.VNF` written in a node template, which is why classification has to walk
`derived_from` rather than compare type names. A parser matching on names produces an empty VNFD
here and reports nothing — that silent failure is what this fixture exists to catch.

## The inheritance chains

```
ExampleCorp.VendorTypeCnf.1_0        -> tosca.nodes.nfv.VNF
ExampleCorp.nodes.WebUnit            -> ExampleCorp.nodes.BaseUnit      -> Vdu.OsContainerDeployableUnit
ExampleCorp.nodes.HardenedContainer  -> ExampleCorp.nodes.BaseContainer -> Vdu.OsContainer
ExampleCorp.nodes.ManagementCp       -> tosca.nodes.nfv.VduCp
ExampleCorp.artifacts.SignedImage    -> tosca.artifacts.nfv.SwImage
```

The artifact type matters as much as the node types: artifacts are selected by type walking
`derived_from`, never by artifact name, so a vendor artifact type still has to be found as the
SwImage of its container.

## Expected parse

```
GET /debug?pkg=ExampleCorp_VendorTypeCnf_vnf_pkg

  MyVnf         ExampleCorp.VendorTypeCnf.1_0        bound -> tosca.nodes.nfv.VNF
  WebUnit       ExampleCorp.nodes.WebUnit            bound -> tosca.nodes.nfv.Vdu.OsContainerDeployableUnit
  WebContainer  ExampleCorp.nodes.HardenedContainer  bound -> tosca.nodes.nfv.Vdu.OsContainer
  MgmtCp        ExampleCorp.nodes.ManagementCp       bound -> tosca.nodes.nfv.VduCp
  unbound: []
```

```
vnfdId           e47d2b60-1c8f-4935-a0d7-b83f6e514c29
vdu              [WebUnit]
osContainerDesc  [WebContainer]
swImageDesc      [(WebContainer, version 3.2)]   <- found through ExampleCorp.artifacts.SignedImage
vduCpd           [MgmtCp]
findings         none
```

The VNF node template is called `MyVnf`, not `VNF`, and is not the first node template in the file.
Neither matters: the node is found by type, not by name or position.
