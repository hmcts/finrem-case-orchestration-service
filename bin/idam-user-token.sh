#!/bin/sh

IMPORTER_USERNAME=${1:-fr_system_user@sharklasers.com}
IMPORTER_PASSWORD=${2:-I87ef91zA!}
IDAM_URI="https://idam-api.aat.platform.hmcts.net"
REDIRECT_URI="https://manage-case.aat.platform.hmcts.net/oauth2/callback"
CLIENT_ID="xuiwebapp"
CLIENT_SECRET="DBssSOq0KKLNBf2z"

code=$(curl ${CURL_OPTS} -u "${IMPORTER_USERNAME}:${IMPORTER_PASSWORD}" -XPOST "${IDAM_URI}/oauth2/authorize?redirect_uri=${REDIRECT_URI}&response_type=code&client_id=${CLIENT_ID}" -d "" | jq -r .code)

curl ${CURL_OPTS} -H "Content-Type: application/x-www-form-urlencoded" -u "${CLIENT_ID}:${CLIENT_SECRET}" -XPOST "${IDAM_URI}/oauth2/token?code=${code}&redirect_uri=${REDIRECT_URI}&grant_type=password" -d "" | jq -r .access_token