#!/bin/bash

# Script to test the bulk scan validation requests that are handled by BulkScanController.transformExceptionRecordIntoCase.
#
# This script assumes the following:
# - finrem-ccd-definitions repository is located in the same parent directory as finrem-case-orchestration
# - the following environment variables are set:
#   - FINREM_CLIENT_SECRET_AAT
#   - CCD_IMPORT_USERNAME_AAT
#   - CCD_IMPORT_PASSWORD_AAT

idamApiHost=https://idam-api.aat.platform.hmcts.net
s2sHost=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal

binFolder=$(dirname "$0")

echo "Requesting IDAM access token"
accessToken="$("$binFolder"/idam-access-token.sh $idamApiHost "$FINREM_CLIENT_SECRET_AAT" "$CCD_IMPORT_USERNAME_AAT" "$CCD_IMPORT_PASSWORD_AAT")"
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to get IDAM access token"
  exit 1
fi

echo "Requesting service token"
serviceToken="$("$binFolder"/s2s-token.sh $s2sHost bulk_scan_orchestrator)"
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to get service token"
  exit 1
fi

curl --request POST -i 'http://localhost:9000/transform-exception-record' \
--header 'Content-Type: application/json' \
--header "Authorization: Bearer $accessToken" \
--header "ServiceAuthorization: $serviceToken" \
 --data '
{
  "case_type_id": "FinancialRemedyContested",
  "id": "LV481297",
  "po_box": "PO 17",
  "form_type": "FormA",
  "ocr_data_fields": [
    {
      "name": "divorceCaseNumber",
      "value": "DD12D12345"
    }
  ]
}
'
