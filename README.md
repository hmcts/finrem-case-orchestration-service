# Financial Remedy Case Orchestration Service

This application orchestrates a workflow based on the requested business requirement.

## Getting started

### Prerequisites

- [JDK 11](https://www.oracle.com/java)

### Building

The project uses [Gradle](https://gradle.org) as a build tool but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build project please execute the following command:

```bash
./gradlew build
```

To get the project to build in IntelliJ IDEA, you have to:

 - Install the Lombok plugin: Preferences -> Plugins
 - Enable Annotation Processing: Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors

### Running the service in isolation

You can run the application by executing following command:

```bash
./gradlew bootRun
```

The application will start locally on: `http://localhost:9000`

### Running the service with CCD and Idam locally (Developers and QA)

The best way to develop and test services in Financial Remedy is to run

```bash
./gradlew bootWithCCD
```
This will spin up finrem-case-orchestration along with an Idam simulator and the base CCD services.

In case you are not logged in you must run
```bash
az acr login --name hmctspublic --subscription DCD-CNP-Prod
az acr login --name hmctsprivate --subscription DCD-CNP-Prod
```
#### Gotchas on macOS
- You must have Docker Desktop running

- The IDAM Simulator runs on port 5000, so it is necessary to disable macOS' AirPlay Receiver server in: 
Settings > Sharing > AirPlay Receiver

### API documentation

API documentation is provided with Swagger. This is available locally at: `http://localhost:9000/swagger-ui.html`

## Setup User/Roles, CCD definition file

### Create User/Roles

To create the users/role run the following command:

```bash
./bin/create-roles-users.sh
```
### Import definition file

To import the Financial Remedy CCD definition file, run the following command:

```bash
./bin/ccd-import-definition.sh <<Finrem_Definition_File_Path>>
```

### Load pba reference data using new endpoints of PRD

To load reference data ( organisation, user and pba account: PBA0222), run the following command.

Go to **bin** folder,

```bash
./load-pba-reference-data.sh
```

## Testing

### Unit tests

To run all unit tests and local functional tests please execute following command:

```bash
./gradlew test
```

### Coding style tests

To run all checks (including unit tests) please execute following command:

```bash
./gradlew check
```

### Mutation tests

To run all mutation tests execute the following command:

```bash
./gradlew pitest
```

### Running functional tests locally pointing to AAT

1. Make a copy of `src/main/resources/example-application-aat.properties` as `src/main/resources/application-aat.properties`
2. Make a copy of `src/functionalTests/resources/example-application-local.properties` as `src/functionalTests/resources/application-local.properties`
3. Replace the `replace_me` secrets in both of the _newly created_ files.
   You can get the values from SCM and Azure secrets key vault (the new files are in .gitignore and should ***not*** be committed to git)
4. Start the app with AAT config using `./gradlew clean bootRunAat`
5. Start the test with AAT config using `./gradlew clean functional`

## Versioning

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
