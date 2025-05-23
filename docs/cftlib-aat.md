# RSE CFT lib AAT
This page provides instructions on how to develop and test `finrem-case-orchestration` in a local development
environment using [cftlib](https://github.com/hmcts/rse-cft-lib). AAT is used to provide some of the dependent services. 

## Prerequisites
1. Docker must be running.
2. You must be connected to the VPN.
3. An `.aat-env` file is needed to provide the environment variables required to point services at AAT. 
As these include Azure secrets, it is not stored in Git. Ask a colleague to provide them or alternatively copy them
from the pods running in AAT.

## Setup
The first time cftlib is run it needs to download Docker images from the Azure Registry. You must therefore login
to Azure as follows:
```bash
az acr login --name hmctspublic --subscription DCD-CNP-Prod
az acr login --name hmctsprivate --subscription DCD-CNP-Prod
```

## Running
```bash
./gradlew bootWithCCD
```

This will start `finrem-case-orchestration` along with CCD common components and Docker containers for
ExUI, PostgreSQL, Elasticsearch and Logstash.

| Application         | URL                   |
|---------------------|-----------------------|
| Manage Cases        | http://localhost:3000 |
| Manage Organisation | http://localhost:3001 |

## Importing CCD definitions
The [finrem-ccd-definitions](https://github.com/hmcts/finrem-ccd-definitions) are imported automatically on
cftlib startup.

In order for the definitions to be imported you must have checked out 
[finrem-ccd-definitions](https://github.com/hmcts/finrem-ccd-definitions) in the same parent directory as
`finrem-case-orchestration`.

If you subsequently change the definitions whilst cftlib is running you can import them with:
```bash
./bin/cftlib-import-ccd-definition-aat.sh [contested | consented]
```

This command will build the local versions of the definitions before importing them. If the import is successful then
the message `Case Definition data successfully imported` will be displayed.

You will need to set the following environment variables for it to work:
```
FINREM_IDAM_CLIENT_SECRET
CCD_IMPORT_USERNAME_AAT
CCD_IMPORT_PASSWORD_AAT
```

## Elasticsearch
Elasticsearch is used by the application to store case data. The Elasticsearch container is started by cftlib.

The Financial Remedy case types have a separate index.

| Case Type | Search URL                                                          | Mapping URL                                                          |
|-----------|---------------------------------------------------------------------|----------------------------------------------------------------------|
| Contested | http://localhost:9200/financialremedycontested_cases-000001/_search | http://localhost:9200/financialremedycontested_cases-000001/_mapping |
| Consented | http://localhost:9200/financialremedymvp2_cases-000001/_search      | http://localhost:9200/financialremedymvp2_cases-000001/_mapping      |

### Reindex
To reindex the Elasticsearch data you can use the following command:
```bash
./bin/elasticsearch/reindex.sh [contested | consented]
```

## Mocking Dependent Services
It is possible to mock the responses from dependent services such as the payment-api service.
[WireMock](https://wiremock.org/) can be incorporated into the local development environment via docker compose.

This is done by setting the CFTLIB_EXTRA_COMPOSE_FILES environment variable to a comma-separated list of Docker Compose files.
If configured then Docker Compose will run on cftlib startup.

For example:
```
CFTLIB_EXTRA_COMPOSE_FILES=docker-compose.yml
```

The environment variable can be set in the `.aat-env` file or in your shell before running `bootWithCCD`.

For more details see:

- [DockerComposeProcessRunner.java](../src/cftlib/java/uk/gov/hmcts/reform/finrem/caseorchestration/DockerComposeProcessRunner.java)
- [docker-compose.yml](../src/cftlib/resources/compose/docker-compose.yml)
