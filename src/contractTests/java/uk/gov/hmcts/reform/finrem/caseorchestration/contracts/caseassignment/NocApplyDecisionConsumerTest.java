package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.caseassignment;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.type.TypeReference;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BristolCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;

@SpringBootTest
public class NocApplyDecisionConsumerTest extends BaseTest {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String ASSIGNEE_ID = "0a5874a4-3f38-4bbd-ba4c";
    private static final Long CASE_ID = 1583841721773828L;
    private static final LocalDate HEARING_DATE = LocalDate.of(2022, 1, 1);
    private static final String TEST_APP_ORG_ID = "appOrgId";
    private static final String TEST_APP_ORG_NAME = "appOrgName";
    private static final String TEST_APP_ORG_ID_NEW = "newAppOrgId";
    private static final String TEST_APP_ORG_NAME_NEW = "newAppOrgName";
    private static final String TEST_RESP_ORG_ID = "respOrgId";
    private static final String TEST_RESP_ORG_NAME = "respOrgName";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    AssignCaseAccessService assignCaseAccessService;
    @MockBean
    IdamService idamService;
    @MockBean
    AssignCaseAccessServiceConfiguration assignCaseAccessServiceConfiguration;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("acc_manageCaseAssignment", "localhost", 4454, this);

    private final CaseDetails caseDetails =
        CaseDetails.builder().id(CASE_ID).caseTypeId(String.valueOf(CaseType.CONTESTED)).data(createCaseData()).build();

    @Pact(provider = "acc_manageCaseAssignment", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        // @formatter:off
        return builder
            .given("A notice of change against case")
            .uponReceiving("A request to Apply Notice of Change decision")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .body(createJsonObject(DecisionRequest.decisionRequest(caseDetails)))
            .path("/noc/apply-decision")
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
                        .stringType("OrganisationID", TEST_APP_ORG_ID)
                        .stringType("OrganisationName", TEST_APP_ORG_NAME))
                    .stringType("OrgPolicyReference","DefendantPolicy")
                    .stringType("OrgPolicyCaseAssignedRole","[Defendant]"))
                .object("OrganisationPolicyField2", op2 -> op2
                    .object("Organisation", org -> org
                        .stringType("OrganisationID", TEST_RESP_ORG_ID)
                        .stringType("OrganisationName", TEST_RESP_ORG_NAME))
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
                        .stringType("OrganisationID", TEST_APP_ORG_ID)
                        .stringType("OrganisationName", TEST_APP_ORG_NAME))
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
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);

        assignCaseAccessService.applyDecision(AUTHORIZATION_TOKEN, caseDetails);
    }

    private String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }

    private Map<String, Object> createCaseData(){
        FinremCaseData caseData = new FinremCaseData();

        caseData.setCcdCaseType(CaseType.CONTESTED);
        caseData.getContactDetailsWrapper().setApplicantFmName("Test");
        caseData.getContactDetailsWrapper().setApplicantLname("Applicant");
        caseData.getContactDetailsWrapper().setRespondentFmName("Test");
        caseData.getContactDetailsWrapper().setRespondentLname("Respondent");
        caseData.setDivorceCaseNumber("DD12D12345");
        caseData.getRegionWrapper().getDefaultCourtList().setBristolCourtList(BristolCourt.BRISTOL_CIVIL_AND_FAMILY_JUSTICE_CENTRE);
        caseData.setHearingDate(HEARING_DATE);
        caseData.getContactDetailsWrapper().setSolicitorReference("Test Sol Reference");
        caseData.getContactDetailsWrapper().setRespondentSolicitorReference("Test Resp Sol Ref");
        caseData.setAdditionalInformationAboutHearing("Test");
        caseData.setHearingTime("1pm");
        caseData.setTimeEstimate("1 hour");
        caseData.setChangeOrganisationRequestField(ChangeOrganisationRequest
            .builder()
            .organisationToRemove(Organisation.builder()
                .organisationID(TEST_APP_ORG_ID)
                .organisationName(TEST_APP_ORG_NAME)
                .build())
            .organisationToAdd(Organisation.builder()
                .organisationID(TEST_APP_ORG_ID_NEW)
                .organisationName(TEST_APP_ORG_NAME_NEW)
                .build())
            .caseRoleId(getApplicantCaseRole())
            .reason("test Reason")
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build());

        return objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {});
    }

    private DynamicList getApplicantCaseRole() {
        DynamicListElement appSolRole = DynamicListElement.builder()
            .code(APP_SOLICITOR_POLICY)
            .label(APP_SOLICITOR_POLICY)
            .build();

        return DynamicList
            .builder()
            .value(appSolRole)
            .listItems(List.of(appSolRole))
            .build();
    }


}
