package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.ccddatastore;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class CaseDataStoreServiceConsumerContractTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String authorizationToken = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    public static final long CASE_ID = 1583841721773828L;
    public static final String CASE_TYPE_ID = "FinancialRemedyMVP2";
    private final String someServiceAuthToken = "someServiceAuthToken";

    @MockBean
    IdamService idamService;

    @Autowired
    CcdDataStoreService ccddDataStoreService;

    @MockBean
    CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("ccd_data_store", "localhost", 8891, this);

    @Pact(provider = "ccd_data_store", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException{
        // @formatter:off
        return builder
            .given("An User exists for a Case")
            .uponReceiving("A Request to remove role for users of the case")
            .method("DELETE")
            .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, AUTHORIZATION_HEADER, authorizationToken)
            .path("/case-users")
            .willRespondWith()
            .status(HttpStatus.SC_NO_CONTENT)
            .toPact();
    }

    @Test
    @PactVerification()
    public void removeRole() throws  JSONException {

        given(idamService.getIdamUserId(anyString())).willReturn(ASSIGNEE_ID);
        given(ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl()).willReturn("http://localhost:8891/case-users");
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);

        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).caseTypeId(CASE_TYPE_ID).createdDate(LocalDateTime.now()).build();

        ccddDataStoreService.removeCreatorRole(caseDetails,authorizationToken);
    }
}