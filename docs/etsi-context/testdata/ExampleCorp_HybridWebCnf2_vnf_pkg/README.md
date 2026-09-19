# ExampleCorp_HybridWebCnf2_vnf_pkg

A hybrid VNF in the sense that matters: **one deployment flavour, two VDUs realized by different
means**.

| VDU | How it is realized | Where its container spec lives |
|---|---|---|
| `CacheVdu` | from an MCIOP (`cache_mciop`), SOL001 Annex A.23 pattern | inside the Helm chart, **not in the VNFD** |
| `WebVdu` | from a `Vdu.OsContainer` (`WebContainer`), SOL001 Annex A.18 pattern | in the VNFD |

## Why this is conformant

`[ETSI NORMATIVE]` IFA011 V5.4.1 clause 7.1.6.2.2 Note 10 is written **per VDU**: when a VDU is
realized as OS containers and `osContainerDesc` is absent, "the MciopProfile associated with the VDU
shall be present". `CacheVdu` takes that branch; `WebVdu` takes the other. Nothing requires the VDUs
of one flavour to agree.

`[ETSI NORMATIVE]` IFA011 clause 7.1.8.2.2 makes `mciopProfile` 0..N, present "if the DF references
(via the vduProfile) containerized workloads based on a MCIOP" — a condition about the flavour
referencing such a workload, not about all of them being one.

## The structural signature

This is the assertion worth writing a test around, because it is the one that breaks first if the
parse is wrong:

```
mciopProfile[].associatedVdu   ⊊   vduProfile[].vduId      (a PROPER subset)
```

and `df[0]` holds two different `lcmRealizationPath` values at the same time.

## Expected parse

```
vdu                CacheVdu -> MCIOP_CISM,  WebVdu -> DIRECT_MCIO_CISM
osContainerDesc    [WebContainer]
swImageDesc        [WebContainer]        <- SOL001 6.8.12.6: the node template name is the id
mciopId            [cache_mciop]
intVirtualLinkDesc [InternalVl]
df[0].vduProfile   [CacheVdu, WebVdu]
df[0].mciopProfile [(cache_mciop, [CacheVdu])]
findings           none
```

Note `CacheVdu` contributes nothing to `osContainerDesc`. That is not a gap: with a Helm-realized
VDU the NFVO cannot learn cpu/memory from the descriptor at all, only `vduProfile.min/max` and
`mcioIdentificationData`. Capacity for that VDU has to come from the CISM after the MCIOP is
processed.

## Contrast with `ExampleCorp_HybridWebCnf_vnf_pkg`

The older package puts **both** an `Vdu.OsContainer` and an `Mciop` on the *same* VDU. That has no
worked example in SOL001 — Annex A.18 is OsContainer only, Annex A.23 is MCIOP only. It still
parses and is kept, but this package is the design to follow.

## Why the trap here is worth a fixture

`[ETSI NORMATIVE]` SOL001 V5.4.1 clause 6.8.13.7 asks a similar question of the **whole service
template**: with a `Vdu.OsContainerDeployableUnit` present "while no node template of type
tosca.nodes.nfv.Vdu.OsContainer is present, at least one node template of type
tosca.nodes.nfv.Mciop shall be present". In a hybrid template `WebContainer` **is** present, so that
premise is false and the rule stays silent — even if the author forgets `cache_mciop` entirely.

Only the per-VDU reading of IFA011 Note 10 catches that. The negative fixture
`docs/etsi-context/testdata-negative/HybridWebCnf2_missing_mciop/` is this package with `cache_mciop`
deleted, and it exists to prove exactly that: one ERROR, rule C2.
