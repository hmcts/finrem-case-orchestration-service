package uk.gov.hmcts.reform.finrem.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.functional.model.ClientAuthorizationCodeResponse;
import uk.gov.hmcts.reform.finrem.functional.model.ClientAuthorizationResponse;
import uk.gov.hmcts.reform.finrem.functional.util.FunctionalTestUtils;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;

@ContextConfiguration(classes = TestContextConfiguration.class)
@Component
public class SolCCDServiceAuthTokenGenerator {

    @Value("${idam.oauth2.client.id}")
    private String clientId = "";

    @Value("${idam.oauth2.client.secret}")
    private String clientSecret = "";

    @Value("${auth.idam.client.redirectUri}")
    private String redirectUri = "";

    @Value("${service.name}")
    private String serviceName = "";

    @Value("${idam.s2s-auth.url}")
    private String baseServiceAuthUrl = "";

    @Value("${auth.idam.client.baseUrl}")
    private String baseServiceOauth2Url = "";

    @Value("${clientCode.authorization}")
    private String clientCodeAuthorization = "";


    @Autowired
    private ServiceAuthTokenGenerator tokenGenerator;

    @Autowired
    protected FunctionalTestUtils utils;


    public String generateServiceToken() {
        return tokenGenerator.generate();
    }

    public String getClientToken() {
        return generateClientToken();
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
            + "/oauth2/token?code=" + code
            + "&client_secret=" + clientSecret
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
            .header("Authorization", clientCodeAuthorization)
            .post(baseServiceOauth2Url
                + "/oauth2/authorize?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri)
            .asString();

        ObjectMapper mapper = new ObjectMapper();

        try {
            code = mapper.readValue(jsonResponse, ClientAuthorizationCodeResponse.class).code;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return code;
    }

}
