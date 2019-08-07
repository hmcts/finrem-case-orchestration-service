package uk.gov.hmcts.reform.finrem.caseorchestration.smoketests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmokeTestConfiguration.class})
public class CaseOrchestrationSmokeTests {

    @Value("${url}")
    private String url;

    @Value("${fees.lookup.endpoint}")
    private String feeLookupEndPoint;

    @Value("${http.timeout}")
    private int connectionTimeOut;

    @Value("${http.requestTimeout}")
    private int socketTimeOut;

    @Value("${http.readTimeout}")
    private int connectionManagerTimeOut;

    private RestAssuredConfig config;
    private ObjectMapper objectMapper = new ObjectMapper();


    @Before
    public void setUp() {
        RestAssured.useRelaxedHTTPSValidation();
        config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", connectionTimeOut)
                        .setParam("http.socket.timeout", socketTimeOut)
                        .setParam("http.connection-manager.timeout", connectionManagerTimeOut));


    }

    @Test
    public void shouldFeeLookUp() throws IOException {
        given().config(config)
                .body(objectMapper.writeValueAsString(getRequestFromFile("/case.json")))
                .headers("Content-Type", "application/json")
                .when()
                .post(url + feeLookupEndPoint)
                .()
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    private  CallbackRequest getRequestFromFile(String fileName) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(fileName)) {
            return objectMapper.readValue(resourceAsStream, CallbackRequest.class);
        }
    }
}
