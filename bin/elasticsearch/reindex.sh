#!/bin/bash
# Reindex Elasticsearch in the cftlib local development environment.
#
# Usage: reindex.sh [contested | consented]
# Takes an optional parameter to define which case type to reindex otherwise reindexes both.
#
# This script assumes the following:
# - finrem-ccd-definitions repository is located in the same parent directory as finrem-case-orchestration
# - the following environment variables are set:
#   - FINREM_CLIENT_SECRET_AAT
#   - CCD_IMPORT_USERNAME_AAT
#   - CCD_IMPORT_PASSWORD_AAT

reindexContested=true
reindexConsented=true

if [[ -n $1 ]]; then
  if [[ $1 != "contested" ]] && [[ $1 != "consented" ]]; then
    echo 1>&2 "Error: expecting argument to be contested or consented, found $1"
    exit 1
  fi

  if [[ $1 = "contested" ]]; then
    reindexConsented=false
  else
    reindexContested=false
  fi
fi

if [[ $reindexContested = true ]]; then
  echo "Reindexing contested cases"
  curl -XDELETE localhost:9200/financialremedycontested_cases-000001
  echo
  ./bin/cftlib-import-ccd-definition-aat.sh contested
  if [[ $? -ne 0 ]]; then
    echo 1>&2 "Error: Failed to import contested CCD definition"
    exit 1
  fi

  echo
  echo "Updating case_data table"
  docker exec -t cftlib-shared-database-pg12-1 \
    psql -U postgres -d datastore -c "update case_data set marked_by_logstash = false where case_type_id = 'FinancialRemedyContested';"
fi

if [[ $reindexConsented = true ]]; then
  echo "Reindexing consented cases"
  echo "Deleting index financialremedymvp2_cases-000001"
  curl -XDELETE localhost:9200/financialremedymvp2_cases-000001
  echo
  ./bin/cftlib-import-ccd-definition-aat.sh consented
  if [[ $? -ne 0 ]]; then
    echo 1>&2 "Error: Failed to import consented CCD definition"
    exit 1
  fi

  echo
  echo "Updating case_data table"
  docker exec -t cftlib-shared-database-pg12-1 \
    psql -U postgres -d datastore -c "update case_data set marked_by_logstash = false where case_type_id = 'FinancialRemedyMVP2';"
fi
