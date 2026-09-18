# ExampleCorp_LcmCnf_vnf_pkg

Two things no other package exercises: **lifecycle management scripts** and **MCIOP deployment
order**.

## 1. Vnflcm operations that actually implement something

`[ETSI NORMATIVE]` SOL001 V5.4.1 clause 6.7.1.1 defines `tosca.interfaces.nfv.Vnflcm` with one
operation per VNF LCM operation, plus preamble and postamble operations formed as `<base>_start` and
`<base>_end`. IFA011 V5.4.1 clause 7.1.13.2 makes `LifeCycleManagementScript.script` M,1 — so only
an operation carrying an `implementation` is a script.

The descriptor declares four operations and produces **three** scripts:

```
Vnflcm.instantiate_start   EVENT_START_INSTANTIATION   bash     Artifacts/Scripts/pre_instantiate.sh
Vnflcm.terminate_end       EVENT_END_TERMINATION       bash     Artifacts/Scripts/post_terminate.sh
Vnflcm.scale_start         EVENT_START_SCALING         python   Artifacts/Scripts/pre_scale.py
```

`instantiate` declares `inputs` and no `implementation`. It states the shape of the parameters, not
that anything runs, and must produce nothing — every bundled MCIOP package declares it the same way.

Script paths are resolved against the package root, exactly like artifact paths. A caller handed one
path rooted at the package and another rooted at `Definitions/` can use neither safely.

`[ASSUMPTION]` Two things here are judgement, not specification. The operation-to-event table:
SOL001 names the operations and IFA011 names the events, but neither prints a lookup between them.
And `scriptDsl`, which IFA011 makes M,1 while SOL001 declares no language on a Vnflcm operation —
the file extension is all the descriptor offers.

A base operation such as `instantiate` maps to **no** event. IFA011 describes those values as an
"external stimulus detected on a VNFM reference point", and inventing an `EVENT_` name for them
would state more than the specification does.

## 2. Ordered MCIOPs

`[ETSI NORMATIVE]` SOL001 clause 6.8.14.7: *"The dependency requirement as defined in
TOSCA-Simple-Profile-YAML-v1.3 may be used towards other Mciop nodes to express the order of
deployment."*

`app_mciop` declares `dependency: db_mciop`, so the database chart is installed first:

```
db_mciop    deploymentOrder 0    associatedVdu [DbVdu]
app_mciop   deploymentOrder 1    associatedVdu [AppVdu]
```

The requirement comes from `tosca.nodes.Root`, not from the `Mciop` node type, so it cannot appear
on any class generated from the ETSI type file — it has to be read off the node template as written.
That is the whole reason this package exists.

`[ASSUMPTION]` The number. SOL001 says the requirement expresses an order; it does not say how that
order becomes the integer IFA011 clause 7.1.8.20.2 calls `deploymentOrder`. A topological rank from
zero is used: an MCIOP depending on nothing is 0, one depending on others is one past the highest of
them, and MCIOPs at the same rank have no ordering between them.

A flavour whose MCIOPs declare no dependency gets **no** `deploymentOrder` at all, rather than every
profile claiming rank 0 — saying nothing is more accurate than saying they are all first. A cycle
produces rule C30 and no order.

Expected findings: **none**.
