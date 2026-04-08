#!/bin/sh

IMPORTER_USERNAME=${1:-ccd.docker.default@hmcts.net}
IMPORTER_PASSWORD=${2:-Pa55word11}
IDAM_URI="http://localhost:5000"
REDIRECT_URI="http://localhost:3451/oauth2redirect"
CLIENT_ID="ccd_gateway"
CLIENT_SECRET="ccd_gateway_secret"

curl --insecure --fail --show-error --silent -X POST \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "username=${IMPORTER_USERNAME}" \
  --data-urlencode "password=${IMPORTER_PASSWORD}" \
  --data-urlencode "client_id=${CLIENT_ID}" \
  --data-urlencode "client_secret=${CLIENT_SECRET}" \
  --data-urlencode "redirect_uri=${REDIRECT_URI}" \
  --data-urlencode "scope=openid profile roles" \
  "${IDAM_URI}/o/token" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .access_token
  