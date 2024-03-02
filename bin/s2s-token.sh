#!/bin/bash
# usage: s2s-token.sh S2S-host [microservice]

s2sHost=$1
microservice=${2:-ccd_gw}

curl --silent --location "$s2sHost/testing-support/lease" \
--header 'Content-Type: application/json' \
--data "{
    \"microservice\": \"$microservice\"
}"
