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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.DecisionRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.IOException;
import java.time.LocalDateTime;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class NocApplyDecisionConsumerTest extends BaseTest {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SOME_SERVICE_AUTH_TOKEN = "SOME_SERVICE_AUTH_TOKEN";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    private static final Long CASE_ID = 1583841721773828L;
    private static final String CASE_TYPE_ID = "FinancialRemedyMVP2";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    AssignCaseAccessService assignCaseAccessService;
    @MockBean
    IdamService idamService;
    @MockBean
    AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("acc_manageCaseAssignment", "localhost", 8889, this);

    private final CaseDetails caseDetails =
        CaseDetails.builder().id(CASE_ID).caseTypeId(CASE_TYPE_ID).build();

    @Pact(provider = "acc_manageCaseAssignment", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        return builder
            .given("Given a solicitor against case")
            .uponReceiving("A request to Apply Notice of Change decision")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SOME_SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(DecisionRequest.decisionRequest(caseDetails)))
            .path("/noc/apply-decision")
            .query("use_user_token=true")
            .willRespondWith()
            .body(buildANCDecisionResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private DslPart buildANCDecisionResponseDsl() {
        return newJsonBody((o) -> {
            o.object("data", ob -> ob
                .stringType("TextField", "TextFieldValue")
                .stringType("EmailField", "aca72@gmail.com")
                .numberType("NumberField", 123)
                .object("OrganisationPolicyField1", op1 -> op1
                    .object("Organisation", org -> org
                        .stringType("OrganisationID", "orgId")
                        .stringType("OrganisationName", "orgName"))
                    .stringType("OrgPolicyReference","DefendantPolicy")
                    .stringType("OrgPolicyCaseAssignedRole","[Defendant]"))
                .object("OrganisationPolicyField2", op2 -> op2
                    .object("Organisation", org -> org
                        .stringType("OrganisationID", "QUK822N")
                        .stringType("OrganisationName", "SomeOrg"))
                    .stringType("OrgPolicyReference","ClaimantPolicy")
                    .stringType("OrgPolicyCaseAssignedRole","[Claimant]")))
                .object("ChangeOrganisationRequestField", corf -> corf
                    .stringType("Reason", "some reason")
                    .stringType("CaseRoleId", "Solicitor")
                    .stringType("NotesReason", "Some note")
                    .stringType("ApprovalStatus", "Pending")
                    .stringMatcher("date_updated",
                            "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                            "2020-10-06T18:54:48.785+0000")
                    .object("OrganisationToAdd",  orgRem -> orgRem
                        .stringType("OrganisationID", "orgId")
                        .stringType("OrganisationName", "orgName"))
                    .object("OrganisationToRemove",  orgAdd -> orgAdd
                        .stringType("OrganisationID", "orgId")
                        .stringType("OrganisationName", "orgName")))
                .stringMatcher("date_updated",
                    "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                    "2020-10-06T18:54:48.785+0000");
        }).build();
    }

    @Test
    @PactVerification()
    public void verifyApplyNoticeOfChangeDecision() throws IOException, JSONException {

        given(idamService.getIdamUserId(anyString())).willReturn(ASSIGNEE_ID);
        given(authTokenGenerator.generate()).willReturn(SOME_SERVICE_AUTH_TOKEN);

        assignCaseAccessService
            .applyDecision(SOME_SERVICE_AUTH_TOKEN, caseDetails);
    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }
}
