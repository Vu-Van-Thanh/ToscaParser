Placeholder for the OS container image artifact referenced by the
tosca.artifacts.nfv.SwImage artifact "sw_image" on the WebContainer node
template (Definitions/ExampleCorp_HybridWebCnf_df_simple.yaml), per ETSI
GS NFV-SOL 001 V5.4.1 clause 6.3.1 (tosca.artifacts.nfv.SwImage).

Replace this placeholder with the actual container image reference your
build produces (e.g. an OCI image tar exported with `docker save` /
`skopeo copy`, or - per ETSI GS NFV-SOL 018 V5.4.1 clause 4.2.3 - a
reference resolvable through the OCI Distribution Specification API
exposed by the Container Image Registry (CIR)). Keep the checksum in the
VNFD's sw_image artifact properties in sync with whatever file/reference
you put here.
