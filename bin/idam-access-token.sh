#!/bin/bash
# usage: idam-access-token idam-api-host finrem-client-secret username password

if [ "$#" -ne 4 ];
then
  echo "incorrect number of arguments"
  echo "usage: idam-access-token.sh idam-api-host finrem-client-secret username password"
  exit 1
fi

# Script requires jq
if ! command -v jq &> /dev/null
then
    echo "command not found: jq"
    echo "install using: brew install jq"
    exit 1
fi

idamApiHost=$1
clientSecret=$2
username=$3
password=$4
redirectUri="http://localhost:3451/oauth2redirect"
clientId="finrem"

curl --silent --location "${idamApiHost}/o/token" \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode "client_id=${clientId}" \
--data-urlencode "client_secret=${clientSecret}" \
--data-urlencode "redirect_uri=${redirectUri}" \
--data-urlencode "username=${username}" \
--data-urlencode "password=${password}" \
--data-urlencode 'scope=openid profile roles' \
--data-urlencode 'grant_type=password' \
| jq -r .access_token
