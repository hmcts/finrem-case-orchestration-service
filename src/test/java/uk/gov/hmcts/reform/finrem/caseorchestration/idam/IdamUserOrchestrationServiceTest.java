package uk.gov.hmcts.reform.finrem.caseorchestration.idam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdamUserOrchestrationServiceTest {

    private static final String CLIENT_ID = "finrem";
    private static final String CLIENT_SECRET = "configured-secret";
    private static final String TOKEN = "access-token";
    private static final String USER_EMAIL = "staff.admin@example.com";
    private static final String USER_ID = "user-id";

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private OAuth2Configuration oauth2Configuration;

    private IdamUserOrchestrationService service;

    @BeforeEach
    void setUp() {
        service = new IdamUserOrchestrationService(restTemplate, oauth2Configuration, "aat", "profile roles");
    }

    @Test
    void shouldCreateUserUsingClientCredentialsToken() {
        when(oauth2Configuration.getClientId()).thenReturn(CLIENT_ID);
        when(oauth2Configuration.getClientSecret()).thenReturn(CLIENT_SECRET);
        whenIdamCreateFlowSucceeds();

        IdamUserOrchestrationModels.OrchestrationResponse response = service.createUser(createRequest("demo"), null);

        assertThat(response.operation()).isEqualTo("CREATE");
        assertThat(response.environment()).isEqualTo("demo");
        assertThat(response.users()).hasSize(1);
        assertThat(response.users().get(0).id()).isEqualTo(USER_ID);
        assertThat(response.users().get(0).email()).isEqualTo(USER_EMAIL);
        assertThat(response.users().get(0).roleNames()).containsExactly("staff-admin", "caseworker");
        assertThat(response.steps()).extracting(IdamUserOrchestrationModels.Step::name)
            .containsExactly("GET_CLIENT_CREDENTIALS_TOKEN", "CREATE_USER");

        assertTokenRequest("https://idam-web-public.demo.platform.hmcts.net/o/token", CLIENT_SECRET);
        assertCreateUserRequest("https://idam-testing-support-api.demo.platform.hmcts.net/test/idam/users");
    }

    @Test
    void shouldUseClientSecretHeaderOverrideWhenProvided() {
        when(oauth2Configuration.getClientId()).thenReturn(CLIENT_ID);
        whenIdamCreateFlowSucceeds();

        service.createUser(createRequest(null), "override-secret");

        assertTokenRequest("https://idam-web-public.aat.platform.hmcts.net/o/token", "override-secret");
    }

    @Test
    void shouldDeleteUser() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
            .thenReturn(ResponseEntity.noContent().build());

        IdamUserOrchestrationModels.OrchestrationResponse response = service.deleteUser(
            new IdamUserOrchestrationModels.DeleteUserRequest(
                "aat",
                List.of(new IdamUserOrchestrationModels.UserToDelete(USER_EMAIL))
            )
        );

        assertThat(response.operation()).isEqualTo("DELETE");
        assertThat(response.users()).hasSize(1);
        assertThat(response.users().get(0).email()).isEqualTo(USER_EMAIL);
        assertThat(response.users().get(0).deletedEmail()).isEqualTo(USER_EMAIL);
        assertThat(response.steps()).extracting(IdamUserOrchestrationModels.Step::name)
            .containsExactly("DELETE_USER");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
        assertThat(uriCaptor.getValue()).hasToString(
            "https://idam-api.aat.platform.hmcts.net/testing-support/accounts/staff.admin@example.com"
        );
    }

    @Test
    void shouldUpdateUserByDeletingAndCreating() {
        when(oauth2Configuration.getClientId()).thenReturn(CLIENT_ID);
        when(oauth2Configuration.getClientSecret()).thenReturn(CLIENT_SECRET);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)))
            .thenReturn(ResponseEntity.noContent().build());
        whenIdamCreateFlowSucceeds();

        IdamUserOrchestrationModels.OrchestrationResponse response = service.updateUser(
            new IdamUserOrchestrationModels.UpdateUserRequest(
                "aat",
                "updated-password",
                List.of(new IdamUserOrchestrationModels.UserToUpdate(
                    "old.staff.admin@example.com",
                    USER_EMAIL,
                    "Staff",
                    "Admin",
                    List.of("staff-admin", "caseworker")
                ))
            ),
            null
        );

        assertThat(response.operation()).isEqualTo("UPDATE");
        assertThat(response.users()).hasSize(1);
        assertThat(response.users().get(0).deletedEmail()).isEqualTo("old.staff.admin@example.com");
        assertThat(response.users().get(0).id()).isEqualTo(USER_ID);
        assertThat(response.users().get(0).email()).isEqualTo(USER_EMAIL);
        assertThat(response.steps()).extracting(IdamUserOrchestrationModels.Step::name)
            .containsExactly("DELETE_USER", "GET_CLIENT_CREDENTIALS_TOKEN", "CREATE_USER");
    }

    @Test
    void shouldRejectCreateWhenClientSecretIsNotConfiguredOrProvided() {
        when(oauth2Configuration.getClientSecret()).thenReturn("DUMMY_SECRET");

        assertThatThrownBy(() -> service.createUser(createRequest("aat"), null))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(restTemplate, never()).exchange(
            any(URI.class), any(HttpMethod.class), any(HttpEntity.class), anyMapResponse()
        );
    }

    @SuppressWarnings("unchecked")
    private void assertTokenRequest(String expectedUri, String expectedSecret) {
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(2)).exchange(
            uriCaptor.capture(),
            eq(HttpMethod.POST),
            entityCaptor.capture(),
            anyMapResponse()
        );

        assertThat(uriCaptor.getAllValues().get(0)).hasToString(expectedUri);
        assertThat(entityCaptor.getAllValues().get(0).getHeaders().getContentType())
            .isEqualTo(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body =
            (MultiValueMap<String, String>) entityCaptor.getAllValues().get(0).getBody();
        assertThat(body.getFirst("grant_type")).isEqualTo("client_credentials");
        assertThat(body.getFirst("client_id")).isEqualTo(CLIENT_ID);
        assertThat(body.getFirst("client_secret")).isEqualTo(expectedSecret);
        assertThat(body.getFirst("scope")).isEqualTo("profile roles");
    }

    @SuppressWarnings("unchecked")
    private void assertCreateUserRequest(String expectedUri) {
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(2)).exchange(
            uriCaptor.capture(),
            eq(HttpMethod.POST),
            entityCaptor.capture(),
            anyMapResponse()
        );

        assertThat(uriCaptor.getAllValues().get(1)).hasToString(expectedUri);
        assertThat(entityCaptor.getAllValues().get(1).getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
            .isEqualTo("Bearer " + TOKEN);
        assertThat(entityCaptor.getAllValues().get(1).getHeaders().getContentType())
            .isEqualTo(MediaType.APPLICATION_JSON);

        Map<String, Object> body = (Map<String, Object>) entityCaptor.getAllValues().get(1).getBody();
        assertThat(body.get("password")).isEqualTo("test-password");
        assertThat(body.get("user")).isEqualTo(user());
    }

    private void whenIdamCreateFlowSucceeds() {
        ResponseEntity<Map<String, Object>> tokenResponse = ResponseEntity.ok(Map.of("access_token", TOKEN));
        ResponseEntity<Map<String, Object>> createResponse = ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("id", USER_ID, "email", USER_EMAIL));

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), anyMapResponse()))
            .thenReturn(tokenResponse, createResponse);
    }

    private IdamUserOrchestrationModels.CreateUserRequest createRequest(String environment) {
        return new IdamUserOrchestrationModels.CreateUserRequest(environment, "test-password", List.of(user()));
    }

    private IdamUserOrchestrationModels.User user() {
        return new IdamUserOrchestrationModels.User(
            USER_EMAIL,
            "Staff",
            "Admin",
            List.of("staff-admin", "caseworker")
        );
    }

    @SuppressWarnings("unchecked")
    private ParameterizedTypeReference<Map<String, Object>> anyMapResponse() {
        return (ParameterizedTypeReference<Map<String, Object>>) any(ParameterizedTypeReference.class);
    }
}
