package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOC_PARTY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.SOLICITOR_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class AddedSolicitorServiceTest {

    private static final String TEST_APP_ORG_ID = "AppTestId";
    private static final String TEST_APP_ORG_NAME = "AppTestOrgName";
    private static final String APP_SOL_FORENAME = "Applicant";
    private static final String APP_SOL_SURNAME = "Solicitor";
    private static final String APP_SOL_NAME = APP_SOL_FORENAME + " " + APP_SOL_SURNAME;
    private static final String APP_SOL_EMAIL = "appsolicitor@gmail.com";

    private static final String TEST_RESP_ORG_ID = "RespTestId";
    private static final String TEST_RESP_ORG_NAME = "RespTestOrgName";
    private static final String RESP_SOL_FORENAME = "Respondent";
    private static final String RESP_SOL_SURNAME = "Solicitor";
    private static final String RESP_SOL_NAME = RESP_SOL_FORENAME + " " + RESP_SOL_SURNAME;
    private static final String RESP_SOL_EMAIL = "respsolicitor@gmail.com";

    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    private AddedSolicitorService addedSolicitorService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).id(123L).data(new HashMap<>()).build();
    }

    @Test
    public void givenAppSolicitor_whenGetAddedSolicitorAsSolicitor_thenGetCorrectAddedSolicitor() {
        DynamicList appSolRoleList = getApplicantCaseRole();

        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(appSolRoleList);

        UserDetails solicitorToAdd = UserDetails.builder()
            .forename(APP_SOL_FORENAME)
            .surname(APP_SOL_SURNAME)
            .email(APP_SOL_EMAIL)
            .build();

        ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsSolicitor(solicitorToAdd, changeRequest);
        assertEquals(addedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(addedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenRespSolicitor_whenGetAddedSolicitorAsSolicitor_thenGetCorrectAddedSolicitor() {
        DynamicList respSolRoleList = getRespondentCaseRole();
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequest(respSolRoleList);

        UserDetails solicitorToAdd = UserDetails.builder()
            .forename(RESP_SOL_FORENAME)
            .surname(RESP_SOL_SURNAME)
            .email(RESP_SOL_EMAIL)
            .build();

        ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsSolicitor(solicitorToAdd, changeRequest);
        assertEquals(addedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(addedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationID(), TEST_RESP_ORG_ID);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationName(), TEST_RESP_ORG_NAME);
    }

    @Test
    public void givenAppSolicitorContested_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        OrganisationPolicy applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_APP_ORG_ID).organisationName(TEST_APP_ORG_NAME).build())
            .build();
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, applicantOrgPolicy);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        assertEquals(addedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(addedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenAppSolicitorConsented_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        OrganisationPolicy applicantOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_APP_ORG_ID).organisationName(TEST_APP_ORG_NAME).build())
            .build();

        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, applicantOrgPolicy);

        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        assertEquals(addedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(addedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenRespSolicitor_whenGetAddedSolicitorAsCaseworker_thenGetCorrectAddedSolicitor() {
        OrganisationPolicy respondentOrgPolicy = OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_RESP_ORG_ID).organisationName(TEST_RESP_ORG_NAME).build())
            .build();

        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, respondentOrgPolicy);

        ChangedRepresentative addedSolicitor = addedSolicitorService.getAddedSolicitorAsCaseworker(caseDetails);
        assertEquals(addedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(addedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationID(), TEST_RESP_ORG_ID);
        assertEquals(addedSolicitor.getOrganisation().getOrganisationName(), TEST_RESP_ORG_NAME);
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

    private DynamicList getRespondentCaseRole() {
        DynamicListElement respSoleRole = DynamicListElement.builder()
            .code(RESP_SOLICITOR_POLICY)
            .label(RESP_SOLICITOR_POLICY)
            .build();

        return DynamicList
            .builder()
            .value(respSoleRole)
            .listItems(List.of(respSoleRole))
            .build();
    }

    private ChangeOrganisationRequest getApplicantChangeRequest(DynamicList applicantRole) {
        return ChangeOrganisationRequest
            .builder()
            .organisationToAdd(Organisation.builder()
                .organisationID(TEST_APP_ORG_ID)
                .organisationName(TEST_APP_ORG_NAME)
                .build())
            .caseRoleId(applicantRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }

    private ChangeOrganisationRequest getRespondentChangeRequest(DynamicList respondentRole) {
        return ChangeOrganisationRequest
            .builder()
            .organisationToAdd(Organisation.builder()
                .organisationID(TEST_RESP_ORG_ID)
                .organisationName(TEST_RESP_ORG_NAME)
                .build())
            .caseRoleId(respondentRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }
}
