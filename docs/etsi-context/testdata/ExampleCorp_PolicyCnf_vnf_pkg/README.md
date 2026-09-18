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

1. **`scaleInfo` is empty** on both levels although each declares `scale_info` with an aspect and a
   scale level. IFA011 clause 7.1.8.7.2 gives `InstantiationLevel.scaleInfo` 0..N, and the model has
   the field; `PolicyMapper` does not read it.
2. **`scalingAspect[].stepDeltas` is empty** although the descriptor declares `step_deltas`.
3. **`VduScalingAspectDeltas` and `VduInitialDelta` are ignored entirely.** Both are declared here,
   both parse into the type registry, neither reaches a mapper.

All three are valid SOL001. The descriptor is right; the parse is incomplete.
