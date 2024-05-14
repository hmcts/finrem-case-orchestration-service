package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.organisation;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;

import java.io.IOException;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.BDDMockito.given;

public class OrganisationApiFindUserByEmailContractTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String USER_EMAIL_HEADER = "UserEmail";
    private static final String TEST_USER_EMAIL = "test@example.com";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    PrdOrganisationService prdOrganisationService;

    @MockBean
    IdamService idamService;

    @MockBean
    PrdOrganisationConfiguration prdOrganisationConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("rd-professional-api", "localhost", 8080, this);

    @Pact(provider = "rd-professional-api", consumer = "your-consumer-name")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        return builder
            .given("Given a request to find a user by email")
            .uponReceiving("A request to find a user by email")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .headers(USER_EMAIL_HEADER, TEST_USER_EMAIL) // Specify the test user email
            .path("/refdata/external/v1/organisations/users/accountId")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(buildOrganisationUserResponseDsl())
            .toPact();
    }

    private DslPart buildOrganisationUserResponseDsl() {
        return newJsonBody((o) -> {
            o.stringType("userId", "123456");
            o.stringType("firstName", "John");
            o.stringType("lastName", "Doe");
        }).build();
    }

    @Test
    @PactVerification
    public void verifyFindUserByEmail() {
        given(idamService.getUserEmailId(AUTHORIZATION_TOKEN)).willReturn(TEST_USER_EMAIL);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        prdOrganisationService.findUserByEmail(TEST_USER_EMAIL, AUTHORIZATION_TOKEN);

    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }
}