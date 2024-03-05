#!/bin/bash
# Imports the local versions of CCD definition files into an environment running CFTLib authMode = AAT.
# The local versions are automatically generated prior to import.
#
# Usage: cftlib-import-ccd-definition-aat.sh [contested | consented]
# Takes an optional parameter to define which CCD definitions to import otherwise imports both.
#
# This script assumes the following:
# - finrem-ccd-definitions repository is located in the same parent directory as finrem-case-orchestration
# - the following environment variables are set:
#   - FINREM_CLIENT_SECRET_AAT
#   - CCD_IMPORT_USERNAME_AAT
#   - CCD_IMPORT_PASSWORD_AAT

if [[ -z $FINREM_CLIENT_SECRET_AAT ]]; then
  echo 1>&2 "Error: environment variable FINREM_CLIENT_SECRET_AAT not set"
  exit 1
fi

if [[ -z $CCD_IMPORT_USERNAME_AAT ]]; then
  echo 1>&2 "Error: environment variable CCD_IMPORT_USERNAME_AAT not set"
  exit 1
fi

if [[ -z $CCD_IMPORT_PASSWORD_AAT ]]; then
  echo 1>&2 "Error: environment variable CCD_IMPORT_PASSWORD_AAT not set"
  exit 1
fi

importContested=true
importConsented=true

if [[ -n $1 ]]; then
  if [[ $1 != "contested" ]] && [[ $1 != "consented" ]]; then
    echo 1>&2 "Error: expecting argument to be contested or consented, found $1"
    exit 1
  fi

  if [[ $1 = "contested" ]]; then
    importConsented=false
  else
    importContested=false
  fi
fi

idamApiHost=https://idam-api.aat.platform.hmcts.net
s2sHost=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
ccdDefinitionStoreHost=http://localhost:4451

binFolder=$(dirname "$0")

echo "Requesting IDAM access token"
accessToken="$("$binFolder"/idam-access-token.sh $idamApiHost "$FINREM_CLIENT_SECRET_AAT" "$CCD_IMPORT_USERNAME_AAT" "$CCD_IMPORT_PASSWORD_AAT")"

echo "Requesting service token"
serviceToken="$("$binFolder"/s2s-token.sh $s2sHost)"

if [[ ${PWD##*/} = "bin" ]]; then
  cd ..
fi

cd "$(find ../ -name finrem-ccd-definitions -maxdepth 1 -mindepth  1 -type d)" || exit 1

if [[ $importContested = true ]]; then
  yarn generate-excel-local-contested

  contestedFile="./definitions/contested/xlsx/ccd-config-local-contested-base.xlsx"
  echo "Importing $contestedFile"

  curl --location "$ccdDefinitionStoreHost/import" \
  --header "Authorization: Bearer $accessToken" \
  --header "ServiceAuthorization: $serviceToken" \
  --header 'Content-Type: multipart/form-data' \
  --form "file=@$contestedFile"
fi

if [[ $importConsented = true ]]; then
  yarn generate-excel-local-consented

  consentedFile="./definitions/consented/xlsx/ccd-config-local-consented-base.xlsx"
  echo "Importing $consentedFile"

  curl --location "$ccdDefinitionStoreHost/import" \
  --header "Authorization: Bearer $accessToken" \
  --header "ServiceAuthorization: $serviceToken" \
  --header 'Content-Type: multipart/form-data' \
  --form "file=@$consentedFile"
fi
