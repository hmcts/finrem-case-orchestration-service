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
import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.BDDMockito.given;


@SpringBootTest({"ccd.data-store.api.baseurl=http://localhost:8982"})
@TestPropertySource(locations = "classpath:application.properties")
public class SearchUserRolesConsumerTest extends BaseTest {

    private static final String CASE_REFERENCE = "1583841721773828";

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
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("ccdDataStoreAPI_caseAssignedUserRoles", "localhost", 8982, this);


    @Pact(provider = "ccdDataStoreAPI_caseAssignedUserRoles", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentForSearch(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        return builder
            .given("A User Role exists for a Case")
            .uponReceiving("A Request to search user roles")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/case-users/search")
            .body(createJsonObject(buildSearchCaseAssignedUserRolesRequest()))
            .willRespondWith().body(buildCaseAssignedRolesResponse())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private SearchCaseAssignedUserRolesRequest buildSearchCaseAssignedUserRolesRequest() {
        return SearchCaseAssignedUserRolesRequest.builder()
            .caseIds(List.of(CASE_REFERENCE))
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
