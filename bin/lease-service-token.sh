#!/usr/bin/env bash
set -eu
microservice=${1:-finrem_case_orchestration}
curl --insecure --fail --show-error --silent -X POST \
  ${SERVICE_AUTH_PROVIDER_API_BASE_URL:-http://rpe-service-auth-provider-aat.service.core-compute-aat.internal}/testing-support/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'"
  }' \
  -w "\n"