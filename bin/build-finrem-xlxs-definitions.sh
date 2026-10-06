#!/usr/bin/env bash
orchestrator_dir="$PWD"
IS_ENABLE_PROD_CCD_FOR_LOCAL=$1

mkdir "$orchestrator_dir"/build/definitionsToBeImported

cd $(find ../ -name finrem-ccd-definitions -maxdepth 1 -mindepth  1 -type d)

if [ "$IS_ENABLE_PROD_CCD_FOR_LOCAL" = "true" ]; then
    echo "Building local environment with PROD data."
    yarn generate-excel-local-with-prod-all
else
    echo "Building local environment with NON-PROD data."
    yarn generate-excel-local-all
fi

mv definitions/consented/xlsx/ccd-config-local-consented-base.xlsx "$orchestrator_dir"/build/definitionsToBeImported/ccd-config-local-consented-base.xlsx
mv definitions/contested/xlsx/ccd-config-local-contested-base.xlsx "$orchestrator_dir"/build/definitionsToBeImported/ccd-config-local-contested-base.xlsx