Placeholder for the backend-vdu container image referenced by the
tosca.artifacts.nfv.SwImage artifact on Vdu2Container
(Definitions/ExampleCorp_RegularCnf_df_simple.yaml).

Replace this file with a real image reference before on-boarding into an
NFVO/VNFM (e.g. a `docker save`/skopeo-produced image archive, or a
reference resolvable via the CISM's OCI Distribution API per SOL018
clause 4.2.3), and update the sw_image artifact's checksum/size/version
properties in the VNFD to match.
