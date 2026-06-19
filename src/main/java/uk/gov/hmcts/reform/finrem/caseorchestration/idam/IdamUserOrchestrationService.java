package uk.gov.hmcts.reform.finrem.caseorchestration.idam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class IdamUserOrchestrationService {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_RESPONSE =
        new ParameterizedTypeReference<>() {
        };
    private static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    private static final String DUMMY_SECRET = "DUMMY_SECRET";
    private static final String IDAM_API = "idam-api";
    private static final String IDAM_TESTING_SUPPORT_API = "idam-testing-support-api";
    private static final String IDAM_WEB_PUBLIC = "idam-web-public";
    private static final String STEP_CREATE_USER = "CREATE_USER";
    private static final String STEP_DELETE_USER = "DELETE_USER";
    private static final String STEP_GET_CLIENT_CREDENTIALS_TOKEN = "GET_CLIENT_CREDENTIALS_TOKEN";
    private static final Pattern ENVIRONMENT_PATTERN = Pattern.compile("^[a-z0-9-]+$");

    private final RestTemplate restTemplate;
    private final OAuth2Configuration oauth2Configuration;
    private final String defaultEnvironment;
    private final String clientCredentialsScope;

    public IdamUserOrchestrationService(RestTemplate restTemplate,
                                        OAuth2Configuration oauth2Configuration,
                                        @Value("${idam.test-support.default-env}") String defaultEnvironment,
                                        @Value("${idam.test-support.client-credentials-scope}") String clientCredentialsScope) {
        this.restTemplate = restTemplate;
        this.oauth2Configuration = oauth2Configuration;
        this.defaultEnvironment = defaultEnvironment;
        this.clientCredentialsScope = clientCredentialsScope;
    }

    public IdamUserOrchestrationModels.OrchestrationResponse createUser(
        IdamUserOrchestrationModels.CreateUserRequest request,
        String clientSecretOverride) {

        String environment = resolveEnvironment(request.environment());
        List<IdamUserOrchestrationModels.Step> steps = new ArrayList<>();
        String accessToken = getClientCredentialsAccessToken(environment, resolveClientSecret(clientSecretOverride), steps);
        List<IdamUserOrchestrationModels.UserResult> users = request.users().stream()
            .map(user -> createdUserResult(createUserInIdam(environment, accessToken, request.password(), user, steps), user, null))
            .toList();

        return new IdamUserOrchestrationModels.OrchestrationResponse("CREATE", environment, users, steps);
    }

    public IdamUserOrchestrationModels.OrchestrationResponse deleteUser(
        IdamUserOrchestrationModels.DeleteUserRequest request) {

        String environment = resolveEnvironment(request.environment());
        List<IdamUserOrchestrationModels.Step> steps = new ArrayList<>();
        List<IdamUserOrchestrationModels.UserResult> users = request.users().stream()
            .map(user -> {
                deleteUserFromIdam(environment, user.email(), steps);
                return new IdamUserOrchestrationModels.UserResult(null, user.email(), user.email(), null);
            })
            .toList();

        return new IdamUserOrchestrationModels.OrchestrationResponse("DELETE", environment, users, steps);
    }

    public IdamUserOrchestrationModels.OrchestrationResponse updateUser(
        IdamUserOrchestrationModels.UpdateUserRequest request,
        String clientSecretOverride) {

        String environment = resolveEnvironment(request.environment());
        List<IdamUserOrchestrationModels.Step> steps = new ArrayList<>();
        String clientSecret = resolveClientSecret(clientSecretOverride);

        request.users().forEach(user -> deleteUserFromIdam(environment, user.existingEmail(), steps));
        String accessToken = getClientCredentialsAccessToken(environment, clientSecret, steps);
        List<IdamUserOrchestrationModels.UserResult> users = request.users().stream()
            .map(user -> createdUserResult(
                createUserInIdam(environment, accessToken, request.password(), user.toUser(), steps),
                user.toUser(),
                user.existingEmail()
            ))
            .toList();

        return new IdamUserOrchestrationModels.OrchestrationResponse("UPDATE", environment, users, steps);
    }

    private Map<String, Object> createUserInIdam(String environment,
                                                 String accessToken,
                                                 String password,
                                                 IdamUserOrchestrationModels.User user,
                                                 List<IdamUserOrchestrationModels.Step> steps) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map<String, Object>> response = exchangeForMap(
            STEP_CREATE_USER,
            buildUri(IDAM_TESTING_SUPPORT_API, environment, "test", "idam", "users"),
            HttpMethod.POST,
            new HttpEntity<>(Map.of("password", password, "user", user), headers)
        );

        requireStatus(response, HttpStatus.CREATED, STEP_CREATE_USER);
        steps.add(step(STEP_CREATE_USER, IDAM_TESTING_SUPPORT_API, response));
        return response.getBody();
    }

    private void deleteUserFromIdam(String environment,
                                    String email,
                                    List<IdamUserOrchestrationModels.Step> steps) {
        ResponseEntity<Void> response = exchangeForVoid(
            STEP_DELETE_USER,
            buildUri(IDAM_API, environment, "testing-support", "accounts", email),
            HttpMethod.DELETE,
            HttpEntity.EMPTY
        );

        requireStatus(response, HttpStatus.NO_CONTENT, STEP_DELETE_USER);
        steps.add(step(STEP_DELETE_USER, IDAM_API, response));
    }

    private String getClientCredentialsAccessToken(String environment,
                                                   String clientSecret,
                                                   List<IdamUserOrchestrationModels.Step> steps) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", CLIENT_CREDENTIALS_GRANT_TYPE);
        body.add("client_id", oauth2Configuration.getClientId());
        body.add("client_secret", clientSecret);
        body.add("scope", clientCredentialsScope);

        ResponseEntity<Map<String, Object>> response = exchangeForMap(
            STEP_GET_CLIENT_CREDENTIALS_TOKEN,
            buildUri(IDAM_WEB_PUBLIC, environment, "o", "token"),
            HttpMethod.POST,
            new HttpEntity<>(body, headers)
        );

        requireStatus(response, HttpStatus.OK, STEP_GET_CLIENT_CREDENTIALS_TOKEN);
        steps.add(step(STEP_GET_CLIENT_CREDENTIALS_TOKEN, IDAM_WEB_PUBLIC, response));

        String accessToken = stringValue(response.getBody(), "access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "IDAM client credentials token response did not contain an access_token"
            );
        }

        return accessToken;
    }

    private ResponseEntity<Map<String, Object>> exchangeForMap(String step,
                                                               URI uri,
                                                               HttpMethod method,
                                                               HttpEntity<?> entity) {
        try {
            return restTemplate.exchange(uri, method, entity, MAP_RESPONSE);
        } catch (HttpStatusCodeException exception) {
            throw badGateway(step, exception.getStatusCode().value(), exception);
        } catch (RestClientException exception) {
            throw badGateway(step, null, exception);
        }
    }

    private ResponseEntity<Void> exchangeForVoid(String step,
                                                 URI uri,
                                                 HttpMethod method,
                                                 HttpEntity<?> entity) {
        try {
            return restTemplate.exchange(uri, method, entity, Void.class);
        } catch (HttpStatusCodeException exception) {
            throw badGateway(step, exception.getStatusCode().value(), exception);
        } catch (RestClientException exception) {
            throw badGateway(step, null, exception);
        }
    }

    private ResponseStatusException badGateway(String step, Integer statusCode, RestClientException exception) {
        String message = statusCode == null
            ? String.format("IDAM test-user orchestration step %s failed", step)
            : String.format("IDAM test-user orchestration step %s failed with status %s", step, statusCode);
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message, exception);
    }

    private void requireStatus(ResponseEntity<?> response, HttpStatus expectedStatus, String step) {
        if (response.getStatusCode().value() != expectedStatus.value()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                String.format("IDAM test-user orchestration step %s returned %s instead of %s",
                    step, response.getStatusCode().value(), expectedStatus.value())
            );
        }
    }

    private URI buildUri(String serviceName, String environment, String... pathSegments) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(String.format("%s.%s.platform.hmcts.net", serviceName, environment));

        for (String pathSegment : pathSegments) {
            builder.pathSegment(pathSegment);
        }

        return builder.build().toUri();
    }

    private String resolveEnvironment(String requestEnvironment) {
        String environment = StringUtils.defaultIfBlank(requestEnvironment, defaultEnvironment)
            .trim()
            .toLowerCase(Locale.ROOT);

        if (!ENVIRONMENT_PATTERN.matcher(environment).matches()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "environment must contain only letters, numbers and hyphens"
            );
        }

        return environment;
    }

    private String resolveClientSecret(String clientSecretOverride) {
        String clientSecret = StringUtils.defaultIfBlank(clientSecretOverride, oauth2Configuration.getClientSecret());

        if (StringUtils.isBlank(clientSecret) || DUMMY_SECRET.equals(clientSecret)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "IDAM client secret must be supplied with X-IDAM-Client-Secret or configured as FINREM_IDAM_CLIENT_SECRET"
            );
        }

        return clientSecret;
    }

    private IdamUserOrchestrationModels.UserResult createdUserResult(
        Map<String, Object> createdUser,
        IdamUserOrchestrationModels.User requestedUser,
        String deletedEmail) {

        return new IdamUserOrchestrationModels.UserResult(
            stringValue(createdUser, "id"),
            StringUtils.defaultIfBlank(stringValue(createdUser, "email"), requestedUser.email()),
            deletedEmail,
            requestedUser.roleNames()
        );
    }

    private String stringValue(Map<String, Object> response, String key) {
        if (response == null || response.get(key) == null) {
            return null;
        }
        return response.get(key).toString();
    }

    private IdamUserOrchestrationModels.Step step(String name, String target, ResponseEntity<?> response) {
        return new IdamUserOrchestrationModels.Step(name, target, response.getStatusCode().value());
    }
}
