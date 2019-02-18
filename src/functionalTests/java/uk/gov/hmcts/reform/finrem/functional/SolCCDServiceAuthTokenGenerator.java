package uk.gov.hmcts.reform.finrem.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.functional.model.ClientAuthorizationCodeResponse;
import uk.gov.hmcts.reform.finrem.functional.model.ClientAuthorizationResponse;
import uk.gov.hmcts.reform.finrem.functional.util.FunctionalTestUtils;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;

@Component
public class SolCCDServiceAuthTokenGenerator {

    @Value("${idam.oauth2.client.id}")
    private String clientId;

    @Value("${idam.oauth2.client.secret}")
    private String clientSecret;

    @Value("${idam.client.redirectUri}")
    private String redirectUri;

    @Value("${idam.s2s-auth.microservice}")
    private String serviceName;

    @Value("${idam.s2s-auth.url}")
    private String baseServiceAuthUrl;

    @Value("${idam.api.url}")
    private String baseServiceOauth2Url;


    @Autowired
    private ServiceAuthTokenGenerator tokenGenerator;

    @Autowired
    private FunctionalTestUtils utilsl;

    public String generateServiceToken() {
        return tokenGenerator.generate();
    }


    public String getUserId() {
        String clientToken = generateClientToken();

        String withoutSignature = clientToken.substring(0, clientToken.lastIndexOf('.') + 1);
        Claims claims = Jwts.parser().parseClaimsJwt(withoutSignature).getBody();

        return claims.get("id", String.class);
    }

    private String generateClientToken() {
        String code = generateClientCode();
        String token = "";

        String jsonResponse = post(baseServiceOauth2Url
            + "/oauth2/token?code="
            + code
            + "&client_secret="
            + clientSecret
            + "&client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&grant_type=authorization_code")
            .body().asString();

        ObjectMapper mapper = new ObjectMapper();

        try {
            token = mapper.readValue(jsonResponse, ClientAuthorizationResponse.class).accessToken;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return token;
    }

    private String generateClientCode() {
        String code = "";
        String jsonResponse = given()
            .relaxedHTTPSValidation()
            .header("Authorization", "Basic dGVzdEBURVNULkNPTToxMjM=")
            .post(baseServiceOauth2Url
                + "/oauth2/authorize?response_type=code"
                + "&client_id="
                + clientId
                + "&redirect_uri="
                + redirectUri)
            .asString();

        ObjectMapper mapper = new ObjectMapper();

        try {
            code = mapper.readValue(jsonResponse, ClientAuthorizationCodeResponse.class).code;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return code;
    }

    public void createNewUser() {
        given().headers("Content-type", "application/json")
            .relaxedHTTPSValidation()
            .body(utilsl.getJsonFromFile("userCreation.json"))
            .post(baseServiceOauth2Url + "/testing-support/accounts");
    }
}
