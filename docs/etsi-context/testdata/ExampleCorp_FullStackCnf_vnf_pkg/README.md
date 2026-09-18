# ExampleCorp_FullStackCnf_vnf_pkg

A **coverage probe**, not a happy path. Every CNF node type the parser can bind appears once, so
that what the parser does *not* yet do is visible as data rather than as an absence.

## What it declares

| Node template | Type | SOL001 clause |
|---|---|---|
| `AppVdu` | `Vdu.OsContainerDeployableUnit` | 6.8.13 |
| `AppContainer` | `Vdu.OsContainer` | 6.8.12 |
| `AppCp` | `VduCp`, `trunk_mode: true` | 6.8.8 |
| `AppSubCp` | `VduSubCp` riding the trunk | 6.8.11 |
| `AppVipCp` | `VipCp` | 6.8.10 |
| `AppVirtualCp` | `VirtualCp` | 6.8.15 |
| `SignallingVl` | `VnfVirtualLink` | 6.8.9 |
| `AppBlockStorage` | `Vdu.VirtualBlockStorage` | 6.8.4 |
| `AppObjectStorage` | `Vdu.VirtualObjectStorage` | 6.8.5 |
| `AppFileStorage` | `Vdu.VirtualFileStorage` | 6.8.6 |
| `OptionalModule` | `DeployableModule` | 6.8.16 |
| `OamCertificate` | `Certificate` | 6.8.19 |

All 13 node templates **bind**. Check it yourself:

```
GET /debug?pkg=ExampleCorp_FullStackCnf_vnf_pkg    ->  unboundNodeTemplates: []
```

## Expected parse, including what is missing

```
vdu                  1   [AppVdu]
osContainerDesc      1   [AppContainer]
swImageDesc          1   [AppContainer]
vduCpd               2   [AppCp, AppSubCp]      <- VduSubCp derives from VduCp, so it maps too
vnfExtCpd            1   [AppCp]                <- exposed through substitution_mappings
intVirtualLinkDesc   1   [SignallingVl]
virtualStorageDesc   3   [AppBlockStorage BLOCK, AppObjectStorage OBJECT, AppFileStorage FILE]
vipCpd               1   [AppVipCp]    intCpd [AppCp],  vipFunction high_availability
virtualCpd           1   [AppVirtualCp]  vdu [AppVdu]
certificateDesc      1   [OamCertificate]  VNFOAM_CERT
df[0].deployableModule  1  [OptionalModule]  member [AppVdu]
df[0].virtualLinkProfile 1 [SignallingVl]  max/min bitrate carried
findings             none
```

### The one gap left

**`AppSubCp` loses its sub-port detail.** It maps through the ordinary `VduCp` path, so
`segmentation_type`, `segmentation_id` and `trunk_binding` are read into the bound node and then go
no further. IFA011 keeps that information in `Vdu.trunkPort`, which this library does not model.

Everything else this package used to document as missing is now mapped: storage, VipCp, VirtualCp,
DeployableModule, Certificate and the virtual link profile.
