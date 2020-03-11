package uk.gov.hmcts.reform.finrem.functional.idam;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Base64;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@Component
public class IdamUtils implements IdamUserClient {

    @Value("${idam.api.url}")
    private String idamUserBaseUrl;

    @Value("${idam.whitelist.url}")
    private String idamRedirectUri;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Value("${idam.api.secret}")
    private String idamSecret;

    @Value("${idam.api.url.local}")
    private String idamApiLocalUrl;

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        System.out.println("idamCodeUrl() -----> " + idamCodeUrl());

        Response response = RestAssured.given()
            .header(AUTHORIZATION_HEADER, authHeader)
            .relaxedHTTPSValidation()
            .post(idamCodeUrl());

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                + " body: " + response.getBody().prettyPrint());
        }

        String code = response.getBody().path("code");
        System.out.println("code -----> " + code);

        response = RestAssured.given()
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamTokenUrl(code));

        String token = response.getBody().path("access_token");

        System.out.println("token -----> " + token);

        return token;
    }

    public String generateUserTokenWithValidMicroService(String microServiceName) {
        Response response = SerenityRest.given()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .relaxedHTTPSValidation()
                .body(String.format("{\"microservice\": \"%s\"}", microServiceName))
                .post(idamS2sAuthUrl + "/testing-support/lease");

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                    + " body: " + response.getBody().prettyPrint());
        }

        assert response.getStatusCode() == 200 : "Error generating code from IDAM: " + response.getStatusCode();

        return "Bearer " + response.getBody().asString();
    }

    private String idamCodeUrl() {
        String myUrl = idamUserBaseUrl + "/oauth2/authorize"
            + "?response_type=code"
            + "&client_id=finrem"
            + "&redirect_uri=" + idamRedirectUri;

        return myUrl;
    }

    private String idamTokenUrl(String code) {
        String myUrl = idamUserBaseUrl + "/oauth2/token"
            + "?code=" + code
            + "&client_id=finrem"
            + "&client_secret=" + idamSecret
            + "&redirect_uri=" + idamRedirectUri
            + "&grant_type=authorization_code";

        return myUrl;
    }

    public String getClientAuthToken() {
        Response response = RestAssured.given()
                .relaxedHTTPSValidation()
                .post(idamApiLocalUrl + "?role=caseworker-divorce&id=1");
        System.out.println(response.getBody().asString());

        return response.getBody().asString();
    }
}
