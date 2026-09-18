# ETSI VNFD Parser — starter kit

Grounding material for the Java 11 TOSCA→VNFD parser project (CNF flow only). Drop this whole
folder into your repo (e.g. as `docs/etsi-context/`) so Claude Code can read it directly instead of
re-deriving ETSI mapping rules from scratch each session.

## Contents

- `docs/ETSI_TOSCA_to_VNFD_Parsing_Design.md` — the main design reference: node-level TOSCA→IFA011
  mapping table (grounded in SOL001 Table 6.1-1 / Annex A.9.2-1, CNF rows only), the corrected
  hybrid-VNFD design (2 separate VDUs), parsing algorithm outline, compliance checks, open questions.
- `docs/ETSI_VNFD_Helm_Support_and_Parsing.md` — how MCIOP/Helm support works in the VNFD, with a
  documented correction (an earlier "Annex A.22" citation was wrong — see the correction note inside).
- `docs/ETSI_VNFD_Helm_Mapping.md` — the original Helm-chart-only (MCIOP simplified design) VNFD
  mapping writeup.
- `testdata/ExampleCorp_SimpleWebCnf_vnf_pkg/` — CNF package, MCIOP/Helm only, no `Vdu.OsContainer`
  (SOL001 Annex A.23 pattern).
- `testdata/ExampleCorp_HybridWebCnf_vnf_pkg/` — CNF package combining `Vdu.OsContainer` +
  `Mciop` **on the same VDU** (an earlier, since-reconsidered "hybrid" interpretation — kept as a
  fixture, but see the correction note in the Helm_Support doc: the *current* preferred "hybrid"
  design uses 2 separate VDUs instead — not yet built as a package). Includes
  `ExampleCorp_HybridWebCnf_vnf_pkg_PARSED_VNFD.json`, an illustrative hand-built parsed-VNFD result
  for this exact package — useful as an expected-output fixture for a first parser test.
- `testdata/ExampleCorp_RegularCnf_vnf_pkg/` — CNF package, "regular" design: 2 separate VDUs, each
  with an explicit `Vdu.OsContainer` child, no MCIOP/Helm at all (SOL001 Annex A.18 pattern).

## Not included here (add yourself)

- The full ETSI PDF specs (SOL001, IFA011 required; SOL018/SOL003 recommended for context) — add as
  raw PDFs or, better, text-extracted (`pdftotext`) for fast grep.
- `etsi_nfv_sol001_vnfd_types.yaml` — the official TOSCA type-definitions file, download from
  `https://forge.etsi.org/rep/nfv/SOL001/raw/v5.4.1/etsi_nfv_sol001_vnfd_types.yaml`.

## Confirmed scope decisions (carry these into the Claude Code prompt)

- CNF flow only — VM-based VDU (`Vdu.Compute`) is explicitly out of scope.
- Support multiple deployment flavours per VNFD from v1 (one `VnfDf` per service-template file, per
  SOL001 cl. 6.11.3), not just a single flavour.
- TOSCA function support: detect and tag `get_input`/`get_property`/`get_attribute`/`get_artifact`/
  intrinsic functions rather than only accepting literals; `get_input` and `get_attribute` cannot be
  resolved to a final value at parse time (no VNF instance exists yet) — tag them as
  input-bound/runtime-bound instead of computing a value; `get_property`/`get_artifact` chains that
  resolve entirely within the static descriptor CAN be evaluated at parse time.
- SOL004 (CSAR/package structure) has no V5.4.1 release yet — package-level reading (TOSCA.meta,
  relative artifact paths) is convention-based, not ETSI-verified; flag accordingly rather than
  presenting it as normative.
