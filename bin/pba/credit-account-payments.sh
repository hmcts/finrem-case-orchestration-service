#!/bin/bash
# This script is used to make a credit account payment for a financial remedy case.
# It can be used to trigger duplicate payments for testing purposes.
# Usage: ./bin/pba/credit-account-payments.sh <applicant solicitor password> <ccdReference>

idamApiHost=https://idam-api.aat.platform.hmcts.net
s2sHost=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
paymentsApiHost=http://payment-api-aat.service.core-compute-aat.internal
userEmail="fr_applicant_solicitor1@mailinator.com"
userPassword=$1
ccdReference=$2
if [[ -z $userPassword ]]; then
  echo 1>&2 "Error: User password not provided"
  exit 1
fi

if [[ -z $ccdReference ]]; then
  echo 1>&2 "Error: CCD reference not provided"
  exit 1
fi

binFolder=$(dirname "$(dirname "$0")")
echo "Using bin folder: $binFolder"

echo "Requesting finrem_case_orchestration service token"
serviceToken="$("$binFolder"/s2s-token.sh $s2sHost finrem_case_orchestration)"
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to get service token"
  exit 1
fi

echo "Requesting $userEmail IDAM access token"
accessToken="$("$binFolder"/idam-access-token.sh $idamApiHost "$FINREM_CLIENT_SECRET_AAT" "$userEmail" "$userPassword")"
if [[ $? -ne 0 ]]; then
  echo 1>&2 "Error: Failed to get IDAM access token"
  exit 1
fi

response=$(curl -s -w "\n%{http_code}" -X POST "$paymentsApiHost/credit-account-payments" \
  --header "Authorization: Bearer $accessToken" \
  --header "ServiceAuthorization: Bearer $serviceToken" \
  --header 'Content-Type: application/json' \
  --data @- <<EOF
    {
      "account_number": "PBA0089162",
      "amount": 60,
      "ccd_case_number": "$ccdReference",
      "currency": "GBP",
      "customer_reference": "my ref",
      "description": "Financial Remedy Consented Application",
      "fees": [
        {
          "calculated_amount": 60,
          "code": "FEE0228",
          "version": "5",
          "volume": 1
        }
      ],
      "organisation_name": "FinRem-1-Org",
      "service": "FINREM",
      "site_id": "AA09"
    }
EOF
)

body=$(echo "$response" | sed '$d')
status=$(echo "$response" | tail -n1)
echo "Status: $status"
echo "Body: $body"
