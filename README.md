# Financial Remedy Case Orchestration Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=finrem-case-orchestration&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=finrem-case-orchestration) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=finrem-case-orchestration&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=finrem-case-orchestration) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=finrem-case-orchestration&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=finrem-case-orchestration) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=finrem-case-orchestration&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=finrem-case-orchestration) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=finrem-case-orchestration&metric=coverage)](https://sonarcloud.io/summary/new_code?id=finrem-case-orchestration)

## Overview
`finrem-case-orchestration` is a [Spring Boot](https://spring.io/projects/spring-boot) application and is responsible for handling all CCD callbacks
for Financial Remedy cases. It provides the business logic to enable Financial Remedy cases to be progressed through
the justice system.

The CCD definitions supported by this service can be found [here](https://github.com/hmcts/finrem-ccd-definitions).

<p align="center">
  <img src="https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/c4/finrem/images/structurizr-finrem-overview.png" width="800"/>
</p>

## Prerequisites
- [JDK 21](https://openjdk.java.net/)

## Getting Started

### Building
The project uses [Gradle](https://gradle.org) as a build tool, but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build project execute the following command:

```bash
./gradlew build
```

### IntelliJ IDEA
To get the project to build in IntelliJ IDEA, you have to:

- Install the Lombok plugin: Settings -> Plugins
- Enable Annotation Processing: Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors

### Running
The service can be run with 
```bash
./gradlew bootRun
```
The service listens on port 9000 `http://localhost:9000`

However, in order to develop and test `fincase-case-orchestration` you should run the service with the
[cftlib](https://github.com/hmcts/rse-cft-lib) plugin.

### Running with RSE CFT lib
[cftlib](https://github.com/hmcts/rse-cft-lib) is a Gradle plugin that provides a local CCD/ExUI environment 
in which `finrem-case-orchestration` can be developed and tested.

The integration between the plugin and `finrem-case-orchestration` can be found in:
- `build.gradle` - see `bootWithCCD`
- src/cftlib

cftlib can be configured to run either
- using AAT services. See [cftlib AAT](docs/cftlib-aat.md)
- in a pure local environment without any AAT dependencies. See [cftlib Local](docs/cftlib-local.md)

#### Logs
Application logs for the services running locally can be found in `build/cftlib/logs`.

## API documentation
API documentation is provided with Swagger. This is available locally at: `http://localhost:9000/swagger-ui.html`

## Testing
### Unit tests
To run all unit tests and local functional tests execute following command:

```bash
./gradlew test
```

### Coding style tests
To run all checks (including unit tests) execute following command:

```bash
./gradlew check
```

### Mutation tests
To run all mutation tests execute the following command:

```bash
./gradlew pitest
```

### OWASP Dependency Vulnerability Checks
To run the OWASP checks for vulnerabilities in dependencies:

```bash
./gradlew dependencyCheckAggregate
```

### Crons
You can manually run a cron task from the cli:

```
TASK_NAME=[task] java -jar finrem-case-orchestration-service.jar run

# E.g.
TASK_NAME=AddApplicationTypeTask java -jar finrem-case-orchestration-service.jar

# or
TASK_NAME=AddApplicationTypeTask ./gradlew bootRun
```

### Running functional tests locally using CftLib
Ensure you have the following environment variables set:
- `FINREM_CLIENT_SECRET_AAT`
- `AUTH_PROVIDER_SERVICE_CLIENT_KEY`

1. From a terminal window run `./gradlew bootWithCCD`
2. From a separate terminal window run `./gradlew functional`

bootWithCCD starts the service with an active profile of 'local'.
This can be used to determine locally running behavior. Such as:
- Calls to the GOV.UK Notify API only render a preview of an email in the console.

### Running additional tests in the Jenkins PR Pipeline
1. Add one or more appropriate labels to your PR in GitHub. Valid labels are:

- ```enable_security_scan```
- ```enable_fortify_scan```

2. Trigger a build of your PR in Jenkins.  Fortify scans will take place asynchronously as part of the Static Checks/Container Build step.
- Check the Blue Ocean view for live monitoring, and review the logs once complete for any issues.
- As Fortify scans execute during the Static Checks/Container Build step, you will need to ensure this is triggered by making a minor change to the PR, such as bumping the chart version.

## Setting Up Git Hooks

To ensure pre-push checks are run before pushing changes, configure Git to use the custom hooks directory:

```bash
git config core.hooksPath .githooks
```

This will enable the pre-push hook located in the `.githooks` directory to run automatically before every push.

## Versioning
We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
