package uk.gov.hmcts.reform.finrem.functional.idam;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.functional.model.RegisterUserRequest;
import uk.gov.hmcts.reform.finrem.functional.model.UserDetails;
import uk.gov.hmcts.reform.finrem.functional.model.UserGroup;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;

@Slf4j
@Component
public class IdamUtils {

    public static final String CASEWORKER_USERNAME_PREFIX = "test-finrem-caseworker";
    public static final String TESTUSER_MAIL_DOMAIN = "test.org";
    private static final String BEARER_PREFIX = "Bearer ";

    private final Map<String, String> userTokenCache = new ConcurrentHashMap<>();
    private final Map<String, String> serviceTokenCache = new ConcurrentHashMap<>();
    private final Map<String, String> userIdCache = new ConcurrentHashMap<>();

    @Value("${idam.api.url}")
    private String idamUserBaseUrl;

    @Value("${idam.whitelist.url}")
    private String idamRedirectUri;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Value("${idam.api.secret}")
    private String idamSecret;

    List<UserDetails> createdUsers = new ArrayList<>();

    public String generateUserTokenWithNoRoles(String username, String password) {
        String cacheKey = username + ":" + password;
        return userTokenCache.computeIfAbsent(cacheKey, key -> fetchUserToken(username, password));
    }

    public String generateServiceTokenWithValidMicroservice(String microserviceName) {
        return serviceTokenCache.computeIfAbsent(microserviceName, this::fetchServiceToken);
    }

    public String getUserId(String jwt) {
        return userIdCache.computeIfAbsent(jwt, this::fetchUserId);
    }

    private String fetchUserToken(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + Base64.getEncoder()
            .encodeToString(userLoginDetails.getBytes(StandardCharsets.UTF_8));

        int retryCount = 0;
        Response response;
        do {
            response = RestAssured.given()
                .header(AUTHORIZATION_HEADER, authHeader)
                .relaxedHTTPSValidation()
                .post(idamCodeUrl());
            retryCount++;
        } while (response.getStatusCode() > 300 && retryCount <= 3);

        assert response.getStatusCode() < 300
            : String.format("Code generation failed with code: %d, body: %s",
            response.getStatusCode(), response.getBody().prettyPrint());

        String code = response.getBody().path("code");

        response = RestAssured.given()
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamTokenUrl(code));

        assert HttpStatus.valueOf(response.getStatusCode()) == HttpStatus.OK
            : String.format("Token generation failed with code: %d, body: %s",
            response.getStatusCode(), response.getBody().prettyPrint());

        return response.getBody().path("access_token");
    }

    private String fetchServiceToken(String microserviceName) {
        Response response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .relaxedHTTPSValidation()
            .body(String.format("{\"microservice\": \"%s\"}", microserviceName))
            .post(idamS2sAuthUrl + "/testing-support/lease");

        assert response.getStatusCode() < 300
            : String.format("Token generation failed with code: %d, body: %s",
            response.getStatusCode(), response.getBody().prettyPrint());

        assert response.getStatusCode() == HttpStatus.OK.value()
            : "Error generating code from IDAM: " + response.getStatusCode();

        return BEARER_PREFIX + response.getBody().asString();
    }

    private String fetchUserId(String jwt) {
        Response response = SerenityRest.given()
            .header("Authorization", jwt)
            .relaxedHTTPSValidation()
            .get(idamUserBaseUrl + "/details");

        assert response.getStatusCode() < 300
            : String.format("Fetching user id failed with code: %d, body: %s",
            response.getStatusCode(), response.getBody().prettyPrint());

        return response.getBody().path("id").toString();
    }

    public UserDetails createCaseworkerUser() {
        String username = String.format("%s-%s@%s", CASEWORKER_USERNAME_PREFIX, UUID.randomUUID(), TESTUSER_MAIL_DOMAIN);
        String password = "GNU-TP-chapter13";
        String[] roles = new String[]{
            "caseworker-divorce",
            "caseworker-divorce-financialremedy",
            "caseworker-divorce-financialremedy-courtadmin",
            "caseworker-divorce-bulkscan"
        };

        createUser(username, password, "caseworker", roles);

        String authToken = generateUserTokenWithNoRoles(username, password);
        String userId = getUserId(authToken);

        UserDetails userDetails = UserDetails.builder()
            .username(username)
            .emailAddress(username)
            .password(password)
            .authToken(authToken)
            .id(userId)
            .build();

        createdUsers.add(userDetails);

        return userDetails;
    }

    public void createUser(String username, String password, String userGroup, String... roles) {
        List<UserGroup> rolesList = new ArrayList<>();
        Stream.of(roles).forEach(role -> rolesList.add(UserGroup.builder().code(role).build()));
        UserGroup[] rolesArray = new UserGroup[roles.length];

        RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(username)
                .forename("Esme")
                .surname("Weatherwax")
                .password(password)
                .roles(rolesList.toArray(rolesArray))
                .userGroup(UserGroup.builder().code(userGroup).build())
                .build();

        Response response = SerenityRest.given()
            .header("Content-Type", "application/json")
            .relaxedHTTPSValidation()
            .body(registerUserRequest)
            .post(idamCreateUrl());

        assert response.getStatusCode() < 300
            : String.format("Creating user failed, status: %d, body: %s",
            response.getStatusCode(), response.getBody().prettyPrint());

        log.info("Test user created: {}", username);
    }

    public void deleteTestUsers() {
        createdUsers.stream()
            .map(UserDetails::getUsername)
            .forEach(this::deleteTestUser);
    }

    public void clearCaches() {
        userTokenCache.clear();
        serviceTokenCache.clear();
        userIdCache.clear();
    }

    private void deleteTestUser(String username) {
        Response response = SerenityRest.given()
            .relaxedHTTPSValidation()
            .delete(idamDeleteUserUrl(username));

        if (response.getStatusCode() < 300) {
            log.info("Deleted test user {}", username);
        } else {
            log.error("Failed to delete test user {}", username);
        }
    }

    private String idamDeleteUserUrl(String username) {
        return idamUserBaseUrl + "/testing-support/accounts/" + username;
    }

    private String idamCodeUrl() {
        return idamUserBaseUrl + "/oauth2/authorize"
            + "?response_type=code"
            + "&client_id=finrem"
            + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {
        return idamUserBaseUrl + "/o/token"
            + "?code=" + code
            + "&client_id=finrem"
            + "&client_secret=" + idamSecret
            + "&redirect_uri=" + idamRedirectUri
            + "&grant_type=authorization_code";
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }
}
