package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.ccddatastore;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CcdDataStoreServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.RemoveUserRolesRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.RemoveUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdDataStoreService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CREATOR_USER_ROLE;

@SpringBootTest
public class CaseDataStoreServiceConsumerContractTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    public static final long CASE_ID = 1583841721773828L;
    public static final String CASE_TYPE_ID = "FinancialRemedyMVP2";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @MockBean
    IdamService idamService;

    @Autowired
    CcdDataStoreService ccdDataStoreService;

    @MockBean
    CcdDataStoreServiceConfiguration ccdDataStoreServiceConfiguration;

    @Autowired
    ObjectMapper objectMapper;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("ccdDataStoreAPI_caseAssignedUserRoles", "localhost", 8891, this);

    private final CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).caseTypeId(CASE_TYPE_ID).createdDate(LocalDateTime.now()).build();


    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to remove a User Role")
            .method("DELETE")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users")
            .body(createJsonObject(buildRemoveRoleRequest(caseDetails)))
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactVerification()
    public void removeRole() {

        given(idamService.getIdamUserId(anyString())).willReturn(ASSIGNEE_ID);
        given(ccdDataStoreServiceConfiguration.getRemoveCaseRolesUrl()).willReturn("http://localhost:8891/case-users");
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        ccdDataStoreService.removeCreatorRole(caseDetails, AUTHORIZATION_TOKEN);
    }

    private RemoveUserRolesRequest buildRemoveRoleRequest(CaseDetails caseDetails) {
        return new RemoveUserRolesRequestMapper().mapToRemoveUserRolesRequest(caseDetails, ASSIGNEE_ID, CREATOR_USER_ROLE);
    }

    private String createJsonObject(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }
}