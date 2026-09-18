# ExampleCorp_PolicyCnf_vnf_pkg

Every policy type the parser knows, in one deployment flavour. Two things this package settles that
no other fixture does.

## 1. Instantiation levels that are read, not invented

`[ETSI NORMATIVE]` IFA011 V5.4.1 clause 7.1.8.2.2 makes `instantiationLevel` M,1..N. A descriptor
with no `InstantiationLevels` policy still needs one, so the parser synthesises a level called
`default` and flags it `_synthesised: true`. Every other fixture takes that path.

Here there are two real levels and a `default_level` naming one of them, which clause 7.1.8.2.2
requires once there is more than one entry.

```
df[0].defaultInstantiationLevelId  small
df[0].instantiationLevel
  small  vduLevel [(ApiVdu,1), (DbVdu,1)]   _synthesised absent
  large  vduLevel [(ApiVdu,6), (DbVdu,3)]   _synthesised absent
```

## 2. An affinity group declared by a policy but consumed on a profile

`[ETSI NORMATIVE]` SOL001 V5.4.1 Table 6.1-1 NOTE 3 puts `affinityOrAntiAffinityGroupId` on the
VduProfile / MciopProfile / VirtualLinkProfile, while the policy names the profiled element as its
target. The assignment therefore travels backwards, from policy to profile — and a `PlacementGroup`
target has to be expanded to its members on the way.

```
df[0].affinityOrAntiAffinityGroup
  api_affinity          AFFINITY       container_namespace
  api_db_anti_affinity  ANTI_AFFINITY  cis_node
df[0].vduProfile
  ApiVdu  [api_affinity, api_db_anti_affinity]   <- api_affinity arrived via the PlacementGroup
  DbVdu   [api_db_anti_affinity]
```

`api_affinity` targets the group `ApiPlacement`, not `ApiVdu` directly. Its appearing on `ApiVdu`
is the proof that group expansion works.

Expected findings: **none**.

## Known gaps this package documents

`VduScalingAspectDeltas` and `VduInitialDelta` are declared here and parse into the type registry,
but no mapper reads them. IFA011 models the deltas as part of the scaling aspect; nothing in this
library carries them yet.

`scale_info` and `step_deltas` used to be dropped as well - both are carried now, which is what
`instantiationLevel[].scaleInfo` and `scalingAspect[].stepDeltas` in the output show.
