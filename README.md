# Financial Remedy Case Orchestration Service

## Overview
`finrem-case-orchestration` is a [Spring Boot](https://spring.io/projects/spring-boot) application and is responsible for handling all CCD callbacks
for Financial Remedy cases. It provides the business logic to enable Financial Remedy cases to be progressed through
the justice system.

The CCD definitions supported by this service can be found [here](https://github.com/hmcts/finrem-ccd-definitions).

<p align="center">
  <img src="https://raw.githubusercontent.com/hmcts/reform-api-docs/master/docs/c4/finrem/images/structurizr-finrem-overview.png" width="800"/>
</p>

## Prerequisites
- [JDK 17](https://www.oracle.com/java)

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

### Running functional tests locally pointing to AAT
1. Make a copy of `src/main/resources/example-application-aat.properties` as `src/main/resources/application-aat.properties`
2. Make a copy of `src/functionalTests/resources/example-application-local.properties` as `src/functionalTests/resources/application-local.properties`
3. Replace the `replace_me` secrets in both of the _newly created_ files.
   You can get the values from SCM and Azure secrets key vault (the new files are in .gitignore and should ***not*** be committed to git)
4. Start the app with AAT config using `./gradlew clean bootRunAat`
5. Start the test with AAT config using `./gradlew clean functional`

### Running additional tests in the Jenkins PR Pipeline
1. Add one or more appropriate labels to your PR in GitHub. Valid labels are:

- ```enable_security_scan```
- ```enable_fortify_scan```

2. Trigger a build of your PR in Jenkins.  Fortify scans will take place asynchronously as part of the Static Checks/Container Build step.
- Check the Blue Ocean view for live monitoring, and review the logs once complete for any issues.
- As Fortify scans execute during the Static Checks/Container Build step, you will need to ensure this is triggered by making a minor change to the PR, such as bumping the chart version.

## Versioning
We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
