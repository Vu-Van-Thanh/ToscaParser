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
virtualStorageDesc   0   <-- GAP
findings             none
```

### Known gaps this package documents

These are **expected results today**, recorded so that a change which starts filling them shows up
as a diff rather than as a surprise:

1. **`virtualStorageDesc` is empty** although three storage node templates are declared and bound.
   `FlavourContext` buckets them into a `storages` list whose accessor has no caller, and no
   `StorageMapper` exists. `AppVdu.virtualStorageDesc` does carry the three ids as strings — the
   references survive, the descriptors do not.
2. **`VipCp` and `VirtualCp` produce nothing.** They bind and land among the connection points, but
   they are not `VduCp`, so no `VduCpd` is built and IFA011 `VipCpd` / `VirtualCpd` are never
   produced. They would only appear if exposed through `substitution_mappings`.
3. **`DeployableModule` and `Certificate` produce nothing.** They bind, then fall off the end of the
   classification chain, which has no `else`. `AppVdu.certificateDesc` does carry the id.
4. **`AppSubCp` loses its sub-port detail.** It maps through the ordinary `VduCp` path, so
   `segmentation_type`, `segmentation_id` and `trunk_binding` are read but not carried into IFA011.

None of these is a fixture error. The descriptor is conformant SOL001; the parse is incomplete.
