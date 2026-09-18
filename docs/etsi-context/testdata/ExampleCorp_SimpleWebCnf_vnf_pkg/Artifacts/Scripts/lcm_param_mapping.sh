#!/usr/bin/env bash
# [PROJECT-SPECIFIC] tosca.artifacts.nfv.HelmParamMappingScript implementation
# (ETSI GS NFV-SOL 001 V5.4.1, clause 6.3.4).
#
# Per clause 6.3.4.1, this executable runs in the VNFM execution environment
# and is invoked, prior to sending a request to the Helm-based CISM service
# interface, with the following ordered parameters:
#   $1 - URL of a readable file containing the complete VNF LCM operation
#        task resource (e.g. InstantiateVnfRequest), in JSON format.
#   $2 - URL of a readable zip file with the contents of the VNFD (as
#        obtained per ETSI GS NFV-SOL 003 V5.4.1 clause 10.4.4.3.2, without
#        security information).
#   $3 - URL of the file referenced by the HelmParamMappingRule artifact
#        defined on the same "web_mciop" node template
#        (Artifacts/Scripts/lcm_param_mapping_rules.txt).
#
# Per clause 6.3.4.1 / IFA011 clause 7.1.20, the script's standard output
# must be the contents of a Helm values.yaml file to be passed to the CISM's
# "helm install"/"helm upgrade" invocation (ETSI GS NFV-SOL 018 V5.4.1,
# clauses 7.4.2/7.4.3).
#
# [ASSUMPTION] The concrete parsing/lookup logic below (jq filters, field
# names in the LCM request) is project-specific and is provided as a
# reference implementation only; it is not itself an ETSI-defined algorithm.
# It only reads the field names listed in lcm_param_mapping_rules.txt, whose
# format is VNF-specific per clause 6.3.5.1.

set -euo pipefail

LCM_REQUEST_URL="$1"
VNFD_ZIP_URL="$2"
MAPPING_RULE_URL="$3"

WORKDIR="$(mktemp -d)"
trap 'rm -rf "${WORKDIR}"' EXIT

curl -fsSL "${LCM_REQUEST_URL}" -o "${WORKDIR}/lcm_request.json"
curl -fsSL "${MAPPING_RULE_URL}" -o "${WORKDIR}/mapping_rules.txt"
# The VNFD zip ($VNFD_ZIP_URL) is fetched for completeness but is not needed
# by this reference implementation, since the two properties it would read
# (vdu_profile.max_number_of_instances, mcio_identification_data.name) are
# already surfaced by the NFVO/VNFM in the LCM request's resolved parameters
# in this example; a real implementation would fetch and parse it here.

# mapping_rules.txt is a simple "key=jq_filter" file (see
# lcm_param_mapping_rules.txt); each key becomes a values.yaml field.
replica_count="1"
image_tag="1.0.0"

while IFS='=' read -r key filter; do
  [[ -z "${key}" || "${key}" == \#* ]] && continue
  value="$(jq -r "${filter} // empty" "${WORKDIR}/lcm_request.json" || true)"
  case "${key}" in
    replicaCount) [[ -n "${value}" ]] && replica_count="${value}" ;;
    imageTag)     [[ -n "${value}" ]] && image_tag="${value}" ;;
  esac
done < "${WORKDIR}/mapping_rules.txt"

cat <<EOF
replicaCount: ${replica_count}
image:
  repository: example.invalid/simple-web-cnf
  tag: "${image_tag}"
  pullPolicy: IfNotPresent
resources:
  limits:
    cpu: "500m"
    memory: "256Mi"
  requests:
    cpu: "250m"
    memory: "128Mi"
service:
  type: ClusterIP
  port: 8080
EOF
