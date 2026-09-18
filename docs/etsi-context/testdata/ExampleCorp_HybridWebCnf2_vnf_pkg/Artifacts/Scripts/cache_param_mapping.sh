#!/usr/bin/env bash
# SOL001 V5.4.1 clause 6.3.4.1 calling convention, three ordered parameters:
#   $1  URL of a file holding the complete task resource (e.g. InstantiateVnfRequest) as JSON
#   $2  URL of a zip holding the VNFD content
#   $3  URL of the HelmParamMappingRule file of the same node template, when one is defined
set -euo pipefail
echo "would render values.yaml from $1 using rules $3"
