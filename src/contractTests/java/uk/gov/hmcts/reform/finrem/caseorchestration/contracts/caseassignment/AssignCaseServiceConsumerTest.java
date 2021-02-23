package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.caseassignment;


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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.IOException;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;

@SpringBootTest
public class AssignCaseServiceConsumerTest extends BaseTest {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String authorizationToken = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String CASE_ID = "1583841721773828";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    private final String someServiceAuthToken = "someServiceAuthToken";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    AssignCaseAccessRequestMapper assignCaseAccessRequestMapper;
    @Autowired
    AssignCaseAccessService assignCaseAccessService;
    @MockBean
    IdamService idamService;
    @MockBean
    AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("acc_manageCaseAssignment", "localhost", 8889, this);


    @Pact(provider = "acc_manageCaseAssignment", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        return builder
            .given("Assign a user to a case")
            .uponReceiving("A request for that case to be assigned")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, someServiceAuthToken, AUTHORIZATION_HEADER, authorizationToken)
            .body(createJsonObject(buildAssignCaseRequest()))
            .path("/case-assignments")
            .willRespondWith()
            .body(buildAssignCasesResponseDsl())
            .status(HttpStatus.SC_CREATED)
            .toPact();
    }

    private DslPart buildAssignCasesResponseDsl() {
        return newJsonBody((o) -> {
            o.stringType("status_message",
                "Roles Role1,Role2 from the organisation policies successfully assigned to the assignee.");
        }).build();
    }

    @Test
    @PactVerification()
    public void verifyCaseAssignment() throws IOException, JSONException {

        given(idamService.getIdamUserId(anyString())).willReturn(ASSIGNEE_ID);
        given(assignCaseAccessServiceConfiguration.getCaseAssignmentsUrl()).willReturn("http://localhost:8889/case-assignments");
        given(authTokenGenerator.generate()).willReturn(someServiceAuthToken);

        assignCaseAccessService
            .assignCaseAccess(CaseDetails.builder().id(Long.parseLong(CASE_ID)).caseTypeId(CASE_TYPE_ID_CONTESTED).build(), authorizationToken);

    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }


    private AssignCaseAccessRequest buildAssignCaseRequest() {
        return AssignCaseAccessRequest
            .builder()
            .case_id(CASE_ID)
            .case_type_id(CASE_TYPE_ID_CONTESTED)
            .assignee_id(ASSIGNEE_ID)
            .build();
    }


}
