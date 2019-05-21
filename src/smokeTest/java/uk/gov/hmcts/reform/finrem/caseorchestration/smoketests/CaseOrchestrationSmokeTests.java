package uk.gov.hmcts.reform.finrem.caseorchestration.smoketests;

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

import static io.restassured.RestAssured.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmokeTestConfiguration.class})
public class CaseOrchestrationSmokeTests {

    @Value("${payment.api.baseurl}")
    private String paymentApiUrl;

    @Value("${fees.lookup.endpoint}")
    private String feeLookupEndPoint;

    @Value("${http.timeout}")
    private int connectionTimeOut;

    @Value("${http.requestTimeout}")
    private int socketTimeOut;

    @Value("${http.readTimeout}")
    private int connectionManagerTimeOut;

    private RestAssuredConfig config;

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
    public void shouldFeeLookUp() {
        given().config(config)
                .when()
                .get(paymentApiUrl + feeLookupEndPoint)
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
