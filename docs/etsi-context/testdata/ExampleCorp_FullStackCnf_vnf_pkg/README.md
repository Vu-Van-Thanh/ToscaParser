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

`AppSubCp` also produces a trunk topology on the VDU:

```
vdu[AppVdu].trunkPort  [{parentPortCpd: AppCp, subportList: [{subportCpd: AppSubCp,
                          segmentationType: vlan, segmentationId: 101}]}]
```

IFA011 clause 7.1.6.11 keeps that on the VDU while SOL001 states the relation on the subport, through
the `trunk_binding` requirement of clause 6.8.11 — so the topology is assembled by reading every
subport and grouping them by the parent they name.

### No gaps left

Every node type this package declares now reaches an information element.
