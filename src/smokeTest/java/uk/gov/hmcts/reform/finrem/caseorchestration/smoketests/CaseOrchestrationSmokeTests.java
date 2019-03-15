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

    @Value("${fees.url}")
    private String feeUrl;

    @Value("${fees.api}")
    private String feesApi;

    @Value("${fees.jurisdiction1}")
    private String jurisdiction1;

    @Value("${fees.jurisdiction2}")
    private String jurisdiction2;

    @Value("${fees.channel}")
    private String channel;

    @Value("${fees.service}")
    private String service;

    @Value("${fees.event}")
    private String event;

    @Value("${fees.keyword}")
    private String keyword;

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
                .queryParam("service", service)
                .queryParam("jurisdiction1", jurisdiction1)
                .queryParam("jurisdiction2", jurisdiction2)
                .queryParam("channel", channel)
                .queryParam("event", event)
                .queryParam("keyword", keyword)
                .get(feeUrl + feesApi)
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
