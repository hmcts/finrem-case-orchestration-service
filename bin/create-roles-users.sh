#!/bin/sh

binFolder=$(dirname "$0")

echo $binFolder

./${binFolder}/idam-create-caseworker.sh caseworker-divorce,caseworker-divorce-financialremedy-solicitor henry_fr_harper@yahoo.com London01 FR Solicitor
./${binFolder}/idam-create-caseworker.sh caseworker-divorce,caseworker-divorce-financialremedy-courtadmin claire_fr_mumford@yahoo.com London01 FR Caseworker
./${binFolder}/idam-create-caseworker.sh caseworker-divorce,caseworker-divorce-financialremedy-judiciary peter_fr_chapman@yahoo.com London01 FR Judiciary


./${binFolder}/ccd-add-role.sh caseworker-divorce PUBLIC
./${binFolder}/ccd-add-role.sh caseworker-test PUBLIC
./${binFolder}/ccd-add-role.sh caseworker-divorce-financialremedy PUBLIC
./${binFolder}/ccd-add-role.sh caseworker-divorce-financialremedy-solicitor PUBLIC
./${binFolder}/ccd-add-role.sh caseworker-divorce-financialremedy-courtadmin PUBLIC
./${binFolder}/ccd-add-role.sh caseworker-divorce-financialremedy-judiciary PUBLIC
./${binFolder}/ccd-add-role.sh caseworker-divorce-systemupdate PUBLIC

