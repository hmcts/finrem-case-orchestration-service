#!/usr/bin/env bash

orchestrator_dir="$PWD"

mkdir "$orchestrator_dir"/build/definitionsToBeImported

cd $(find ../ -name finrem-ccd-definitions -maxdepth 1 -mindepth  1 -type d)
yarn generate-excel-local-all

mv definitions/consented/xlsx/ccd-config-local-consented-base.xlsx "$orchestrator_dir"/build/definitionsToBeImported/ccd-config-local-consented-base.xlsx
mv definitions/contested/xlsx/ccd-config-local-contested-base.xlsx "$orchestrator_dir"/build/definitionsToBeImported/ccd-config-local-contested-base.xlsx