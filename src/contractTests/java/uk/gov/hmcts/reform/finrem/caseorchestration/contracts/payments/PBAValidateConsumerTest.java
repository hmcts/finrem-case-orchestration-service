package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAValidationClient;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@SpringBootTest({"pba.validation.url: http://localhost:8891"})
@TestPropertySource(locations = "classpath:application.properties")
public class PBAValidateConsumerTest extends BaseTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String USER_EMAIL = "UserEmail";

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    PBAValidationClient pbaValidationClient;
    @MockBean
    IdamService idamServiceMock;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("referenceData_organisationalExternalPbas", "localhost", 8891, this);


    @Pact(provider = "referenceData_organisationalExternalPbas", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePbaValidatePactFragment(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("Pbas organisational data exists for identifier " + ORGANISATION_EMAIL)
            .uponReceiving("a request for information for that organisation's pbas")
            .path("/refdata/external/v1/organisations/pbas")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN, USER_EMAIL, ORGANISATION_EMAIL)
            .willRespondWith()
            .status(200)
            .body(buildOrganisationalResponsePactDsl())
            .toPact();
    }

    @Test
    @PactVerification(fragment = "generatePbaValidatePactFragment")
    public void verifyPbaValidatePact() {

        when(idamServiceMock.getUserEmailId(SOME_AUTHORIZATION_TOKEN)).thenReturn(ORGANISATION_EMAIL);
        when(authTokenGenerator.generate()).thenReturn(SOME_SERVICE_AUTHORIZATION_TOKEN);
        PBAValidationResponse pbaValidationResponse = pbaValidationClient.isPBAValid(SOME_AUTHORIZATION_TOKEN, "paymentAccountA1");
        assertThat(pbaValidationResponse.isPbaNumberValid(), equalTo(Boolean.TRUE));
    }

    private DslPart buildOrganisationalResponsePactDsl() {
        return newJsonBody(o -> {
            o.object("organisationEntityResponse", ob -> ob
                .stringType("organisationIdentifier",
                    ORGANISATION_EMAIL)
                .stringMatcher("status",
                    "PENDING|ACTIVE|BLOCKED|DELETED", "ACTIVE")
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", true)
                .stringType("companyNumber", "123456")
                .stringType("companyUrl", "somecompany@org.com")
                .array("paymentAccount", pa ->
                    pa.stringType("paymentAccountA1"))
                .object("superUser", su -> su
                    .stringType("firstName", "firstName")
                    .stringType("lastName", "lastName")
                    .stringType("email", "emailAddress"))
            );
        }).build();
    }
}
