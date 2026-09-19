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
- `testdata/` - ten VNF packages. The first three came with this starter kit; the rest were built
  later to reach code paths nothing else touched. Each has its own `README.md` stating what it
  proves and what the parse is expected to produce.

  | Package | What it is for |
  |---|---|
  | `ExampleCorp_SimpleWebCnf_vnf_pkg` | MCIOP/Helm only, no `Vdu.OsContainer` (SOL001 Annex A.23) |
  | `ExampleCorp_RegularCnf_vnf_pkg` | 2 VDUs, each with an explicit `Vdu.OsContainer`, no Helm (Annex A.18) |
  | `ExampleCorp_HybridWebCnf_vnf_pkg` | `Vdu.OsContainer` **and** `Mciop` on the same VDU - an earlier reading, no ETSI worked example; kept, superseded by HybridWebCnf2 |
  | `ExampleCorp_HybridWebCnf2_vnf_pkg` | the hybrid design actually followed: one flavour, two VDUs on different realization paths |
  | `ExampleCorp_MultiDfCnf_vnf_pkg` | the two-level design of SOL001 cl. 6.11.2, two flavours, one `Mciop` with two `associatedVdu` |
  | `ExampleCorp_FullStackCnf_vnf_pkg` | every remaining CNF node type; documents which ones reach no mapper |
  | `ExampleCorp_PolicyCnf_vnf_pkg` | every policy type; real instantiation levels instead of a synthesised one |
  | `ExampleCorp_FunctionsCnf_vnf_pkg` | every TOSCA function of cl. 5.9, plus conformant and non-conformant scalar units |
  | `ExampleCorp_VendorTypeCnf_vnf_pkg` | vendor types two levels below the ETSI ones, so nothing can be matched by name |
  | `ExampleCorp_LcmCnf_vnf_pkg` | Vnflcm scripts and MCIOP deployment order, both read from places the type file cannot express |

  Non-conformant packages live separately, in `src/test/resources/negative/` - one per broken SHALL,
  with a README of their own.

- `etsi-vnfd-parser-demo`, a separate repository, consumes the parser as a library and serves any of
  these packages as JSON over HTTP. It keeps its own copies, so changing a package here does not
  change what it serves.


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
