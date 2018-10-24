# Financial Remedy callback services POC

### Starting

Start the service as usual with Spring Boot

```bash
./gradlew clean bootRun
```
This will run the service on the port specified in application.properties: 9000

Start CCD using docker-compose:
**https://github.com/hmcts/ccd-docker**

```bash
./compose-frontend.sh up -d
```

Run the unit tests :

``` /gradlew test```


### Mutation tests
 To run all mutation tests execute the following command:
 ```
/gradlew pitest
 ```
 
In the Excel definition file specify a callback for some event and put the following url:
host.docker.internal:9000/caseprogression/case-added

Import the file and attach the debugger to FR service to see the request data coming to the service.

Trigger the event the callback is attached to and see the payload. Happy coding!

## LICENSE

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
