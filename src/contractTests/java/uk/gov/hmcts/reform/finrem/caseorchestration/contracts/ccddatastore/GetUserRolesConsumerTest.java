package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.ccddatastore;

import au.com.dius.pact.consumer.dsl.DslPart;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.time.LocalDateTime;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.BDDMockito.given;


@SpringBootTest({"ccd.data-store.api.baseurl=http://localhost:8981"})
@TestPropertySource(locations = "classpath:application.properties")
public class GetUserRolesConsumerTest extends BaseTest {

    @Autowired
    AssignCaseAccessService assignCaseAccessService;
    @Autowired
    CaseAssignedRoleService caseAssignedRoleService;
    @Autowired
    DataStoreClient dataStoreClient;
    @MockitoBean
    CaseDataService caseDataService;
    @MockitoBean
    IdamService idamService;

    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    SystemUserService systemUserService;

    @MockitoBean
    AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    private static final long CASE_ID = 1583841721773828L;
    private static final String CASE_TYPE_ID = "FinancialRemedyMVP2";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    private final CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).caseTypeId(CASE_TYPE_ID).createdDate(LocalDateTime.now()).build();

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("ccdDataStoreAPI_caseAssignedUserRoles", "localhost", 8981, this);

    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to get user roles")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users")
            .matchQuery("case_ids", "1583841721773828", "1583841721773828")
            .matchQuery("user_ids", "0a5874a4-3f38-4bbd-ba4c", "0a5874a4-3f38-4bbd-ba4c")
            .willRespondWith().body(buildCaseAssignedRolesResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactVerification(fragment = "generatePactFragment")
    public void verifyGetUserRoles() {
        given(idamService.getIdamUserId(AUTHORIZATION_TOKEN)).willReturn(ASSIGNEE_ID);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        caseAssignedRoleService.getCaseAssignedUserRole(caseDetails.getId().toString(), AUTHORIZATION_TOKEN);
    }

    private DslPart buildCaseAssignedRolesResponse() {
        return newJsonBody(o -> {
            o.array("case_users", a -> {
                a.object(b -> {
                    b.stringType("case_id", "1583841721773828");
                    b.stringType("user_id", "0a5874a4-3f38-4bbd-ba4c");
                    b.stringType("case_role", "[CREATOR]");
                });
            });
        }).build();
    }
}
