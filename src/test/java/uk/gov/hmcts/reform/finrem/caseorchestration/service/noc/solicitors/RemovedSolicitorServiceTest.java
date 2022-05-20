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

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
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
public class RemovedSolicitorServiceTest {

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

    @Mock
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;

    @Mock
    private CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;

    @InjectMocks
    private RemovedSolicitorService removedSolicitorService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDetails = CaseDetails.builder().caseTypeId(CASE_TYPE_ID_CONTESTED).id(123L).data(new HashMap<>()).build();
    }

    @Test
    public void givenRemovedAppSolicitorContested_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);
        assertEquals(removedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenRemovedAppSolicitorConsented_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(removedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenRemovedNonDigitalAppSolicitor_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequestWithoutRemoved(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(removedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    public void givenRemovedRespSolicitorContested_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequest(getRespondentCaseRole());
        caseDetails.getData().put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(removedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_RESP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_RESP_ORG_NAME);
    }

    @Test
    public void givenRemovedRespSolicitorConsented_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequest(getRespondentCaseRole());
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(removedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_RESP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_RESP_ORG_NAME);
    }

    @Test
    public void givenRemovedNonDigitalRespSolicitor_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequestWithoutRemoved(getRespondentCaseRole());
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(removedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    public void givenRemovedAppSolicitorContested_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails);
        assertEquals(removedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenRemovedAppSolicitorConsented_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails);

        assertEquals(removedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_APP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_APP_ORG_NAME);
    }

    @Test
    public void givenRemovedNonDigitalAppSolicitor_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails);

        assertEquals(removedSolicitor.getName(), APP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), APP_SOL_EMAIL);
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    public void givenRemovedRespSolicitorContested_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails);

        assertEquals(removedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_RESP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_RESP_ORG_NAME);
    }

    @Test
    public void givenRemovedRespSolicitorConsented_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails);

        assertEquals(removedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationID(), TEST_RESP_ORG_ID);
        assertEquals(removedSolicitor.getOrganisation().getOrganisationName(), TEST_RESP_ORG_NAME);
    }

    @Test
    public void givenRemovedNonDigitalRespSolicitor_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails);

        assertEquals(removedSolicitor.getName(), RESP_SOL_NAME);
        assertEquals(removedSolicitor.getEmail(), RESP_SOL_EMAIL);
        assertNull(removedSolicitor.getOrganisation());
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
            .organisationToRemove(Organisation.builder()
                .organisationID(TEST_APP_ORG_ID)
                .organisationName(TEST_APP_ORG_NAME)
                .build())
            .caseRoleId(applicantRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }

    private ChangeOrganisationRequest getApplicantChangeRequestWithoutRemoved(DynamicList applicantRole) {
        return ChangeOrganisationRequest
            .builder()
            .caseRoleId(applicantRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }

    private ChangeOrganisationRequest getRespondentChangeRequest(DynamicList respondentRole) {
        return ChangeOrganisationRequest
            .builder()
            .organisationToRemove(Organisation.builder()
                .organisationID(TEST_RESP_ORG_ID)
                .organisationName(TEST_RESP_ORG_NAME)
                .build())
            .caseRoleId(respondentRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }

    private ChangeOrganisationRequest getRespondentChangeRequestWithoutRemoved(DynamicList respondentRole) {
        return ChangeOrganisationRequest
            .builder()
            .caseRoleId(respondentRole)
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .build();
    }

    private OrganisationPolicy getApplicantOrgPolicyWithOrganisation() {
        return OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_APP_ORG_ID).organisationName(TEST_APP_ORG_NAME).build())
            .build();

    }

    private OrganisationPolicy getRespondentOrgPolicyWithOrganisation() {
        return OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .organisation(Organisation.builder().organisationID(TEST_RESP_ORG_ID).organisationName(TEST_RESP_ORG_NAME).build())
            .build();
    }

    private OrganisationPolicy getApplicantOrgPolicyWithoutOrganisation() {
        return OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
            .organisation(null)
            .build();

    }

    private OrganisationPolicy getRespondentOrgPolicyWithoutOrganisation() {
        return OrganisationPolicy.builder()
            .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
            .organisation(null)
            .build();
    }
}
