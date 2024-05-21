package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.organisation;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PrdOrganisationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest({"prd.organisations.url=http://localhost:8889"})
public class OrganisationApiFindUserByEmailContractTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String USER_EMAIL_HEADER = "UserEmail";
    private static final String TEST_USER_EMAIL = "test@example.com";

    @MockBean
    SystemUserService systemUserService;

    @Autowired
    PrdOrganisationService prdOrganisationService;

    @MockBean
    IdamService idamService;

    @MockBean
    PrdOrganisationConfiguration prdOrganisationConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("referenceData_organisationalExternalUsers", "localhost", 8889, this);

    @Pact(provider = "referenceData_organisationalExternalUsers", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("User exists")
            .uponReceiving("A request to find a user by email")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN,
                USER_EMAIL_HEADER, TEST_USER_EMAIL)
            .path("/refdata/external/v1/organisations/users/accountId")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(buildOrganisationUserResponseDsl())
            .toPact();
    }

    private DslPart buildOrganisationUserResponseDsl() {
        return newJsonBody(o -> o.stringType("userIdentifier", "123456"))
            .build();
    }

    @Test
    @PactVerification
    public void verifyFindUserByEmail() {
        given(idamService.getUserEmailId(anyString())).willReturn(TEST_USER_EMAIL);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        prdOrganisationService.findUserByEmail(TEST_USER_EMAIL, AUTHORIZATION_TOKEN);
    }
}