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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.DataStoreClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.searchuserrole.SearchCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.BDDMockito.given;


@SpringBootTest({"ccd.data-store.api.baseurl: http://localhost:8982"})
@TestPropertySource(locations = "classpath:application.properties")
public class SearchUserRolesConsumerTest extends BaseTest {

    protected static final String CASE_REFERENCE = "1583841721773828";
    @Autowired
    AssignCaseAccessService assignCaseAccessService;
    @Autowired
    CaseAssignedRoleService caseAssignedRoleService;
    @Autowired
    DataStoreClient dataStoreClient;
    @MockBean
    CaseDataService caseDataService;
    @MockBean
    IdamService idamService;

    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    SystemUserService systemUserService;

    @MockBean
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
    public PactProviderRule mockProvider = new PactProviderRule("ccdDataStoreAPI_caseAssignedUserRoles", "localhost", 8982, this);


    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentForSearch(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("User roles exists for a case and search is called")
            .uponReceiving("A Request to search user roles")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users/search")
            .body(createJsonObject(buildSearchCaseAssignedUserRolesRequest(CASE_REFERENCE)))
            .willRespondWith().body(buildCaseAssignedRolesResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private SearchCaseAssignedUserRolesRequest buildSearchCaseAssignedUserRolesRequest(String caseReference) {
        return SearchCaseAssignedUserRolesRequest.builder()
            .caseIds(List.of(caseReference))
            .build();
    }

    @Test
    @PactVerification(fragment = "generatePactFragmentForSearch")
    public void verifySearchUserRoles() {

        given(systemUserService.getSysUserToken()).willReturn(AUTHORIZATION_TOKEN);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
        given(assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl()).willReturn("http://localhost:8981");
        assignCaseAccessService.searchUserRoles(CASE_REFERENCE);
    }


    private DslPart buildCaseAssignedRolesResponse() {
        return newJsonBody(o -> {
            o.array("case_users", a -> {
                a.object(b -> {
                    b.stringType("case_id", "1583841721773828");
                    b.stringType("case_type_id", "FinancialRemedyMVP2");
                    b.stringType("user_id", "0a5874a4-3f38-4bbd-ba4c");
                    b.stringType("case_role", "[APPSOLICITOR]");
                });
            });
        }).build();
    }

    private String createJsonObject(Object obj) throws IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
