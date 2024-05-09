package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.caseassignment;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AssignCaseAccessServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.AssignCaseAccessRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.DecisionRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.IOException;
import java.time.LocalDateTime;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

@SpringBootTest
public class NocApplyDecisionConsumerTest extends BaseTest {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private final String SOME_SERVICE_AUTH_TOKEN = "SOME_SERVICE_AUTH_TOKEN";
    private static final Long CASE_ID = 1583841721773828L;
    private static final String CASE_TYPE_ID = "FinancialRemedyMVP2";

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private final CaseDetails caseDetails =
        CaseDetails.builder().id(CASE_ID).caseTypeId(CASE_TYPE_ID).createdDate(LocalDateTime.now()).build();


    @Pact(provider = "acc_manageCaseAssignment", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        return builder
            .given("Given a solicitor against case")
            .uponReceiving("A request to Apply Notice of Change decision")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SOME_SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(buildAssignCaseRequest()))
            .path("/noc/apply-decision")
            .query("use_user_token=true")
            .willRespondWith()
            .body(buildAssignCasesResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildAssignCasesResponseDsl() {
        return newJsonBody((o) -> {
            //TODO: Create Dsl for response JSON
            o.stringType("");
        }).build();
    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }

    private DecisionRequest buildAssignCaseRequest() {
        return DecisionRequest
            .builder()
            .caseDetails(caseDetails)
            .build();
    }
}
