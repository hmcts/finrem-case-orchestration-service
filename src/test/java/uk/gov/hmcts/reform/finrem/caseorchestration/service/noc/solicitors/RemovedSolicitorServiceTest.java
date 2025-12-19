package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangedRepresentative;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.NoticeOfChangeParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.util.TestLogs;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG2_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_ORG_ID;
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

@ExtendWith(MockitoExtension.class)
class RemovedSolicitorServiceTest {

    private static final String TEST_APP_ORG_ID = TEST_ORG_ID;
    private static final String TEST_APP_ORG_NAME = "AppTestOrgName";
    private static final String APP_SOL_FORENAME = "Applicant";
    private static final String APP_SOL_SURNAME = "Solicitor";
    private static final String APP_SOL_NAME = APP_SOL_FORENAME + " " + APP_SOL_SURNAME;
    private static final String APP_SOL_EMAIL = "appsolicitor@gmail.com";

    private static final String TEST_RESP_ORG_ID = TEST_ORG2_ID;
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

    @BeforeEach
    void setUp() {
        caseDetails = CaseDetails.builder().caseTypeId(CaseType.CONTESTED.getCcdType()).id(Long.valueOf(CASE_ID)).data(new HashMap<>()).build();
    }

    @Test
    void givenRemovedNotRepresented_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitorAsNull() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);
        assertNull(removedSolicitor);
    }

    @Test
    void givenRemovedNotRepresented_whenGetRemovedSolicitorAsSolicitorButCaseRoleIdIsNull_thenReturnCorrectRemovedSolicitorAsNull() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(null);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);
        assertNull(removedSolicitor);
    }

    @Test
    void givenRemovedAppSolicitorContested_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);
        assertEquals(APP_SOL_NAME, removedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_APP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_APP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedAppSolicitorConsented_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequest(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(APP_SOL_NAME, removedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_APP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_APP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedNonDigitalAppSolicitor_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getApplicantChangeRequestWithoutRemoved(getApplicantCaseRole());
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(APP_SOL_NAME, removedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, removedSolicitor.getEmail());
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    void givenRemovedRespSolicitorContested_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequest(getRespondentCaseRole());
        caseDetails.getData().put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(RESP_SOL_NAME, removedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_RESP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_RESP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedRespSolicitorConsented_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequest(getRespondentCaseRole());
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(RESP_SOL_NAME, removedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_RESP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_RESP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedNonDigitalRespSolicitor_whenGetRemovedSolicitorAsSolicitor_thenReturnCorrectRemovedSolicitor() {
        ChangeOrganisationRequest changeRequest = getRespondentChangeRequestWithoutRemoved(getRespondentCaseRole());
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);

        ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsSolicitor(caseDetails,
            changeRequest);

        assertEquals(RESP_SOL_NAME, removedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, removedSolicitor.getEmail());
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    void givenRemovedAppSolicitorContested_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONTESTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(CONTESTED_SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(false);

        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails,
            true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, true);
        verify(caseDataService, times(2)).isConsentedApplication(caseDetails);

        assertEquals(APP_SOL_NAME, removedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_APP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_APP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedAppSolicitorConsented_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);

        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails,
            true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, true);
        verify(caseDataService, times(2)).isConsentedApplication(caseDetails);

        assertEquals(APP_SOL_NAME, removedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_APP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_APP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedNonDigitalAppSolicitor_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(APPLICANT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY, getApplicantOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(CONSENTED_SOLICITOR_NAME, APP_SOL_NAME);
        caseDetails.getData().put(SOLICITOR_EMAIL, APP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, APPLICANT);

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(caseDetails, true)).thenReturn(true);
        when(caseDataService.isConsentedApplication(caseDetails)).thenReturn(true);
        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails,
            true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, true);

        assertEquals(APP_SOL_NAME, removedSolicitor.getName());
        assertEquals(APP_SOL_EMAIL, removedSolicitor.getEmail());
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    void givenRemovedRespSolicitorContested_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(CONTESTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);
        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails,
            false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, false);

        assertEquals(RESP_SOL_NAME, removedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_RESP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_RESP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedRespSolicitorConsented_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);
        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails,
            false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, false);

        assertEquals(RESP_SOL_NAME, removedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, removedSolicitor.getEmail());
        assertEquals(TEST_RESP_ORG_ID, removedSolicitor.getOrganisation().getOrganisationID());
        assertEquals(TEST_RESP_ORG_NAME, removedSolicitor.getOrganisation().getOrganisationName());
    }

    @Test
    void givenRemovedNonDigitalRespSolicitor_whenGetRemovedSolicitorAsCaseworker_thenReturnCorrectRemovedSolicitor() {
        caseDetails.getData().put(CONSENTED_RESPONDENT_REPRESENTED, YES_VALUE);
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY, getRespondentOrgPolicyWithoutOrganisation());
        caseDetails.getData().put(RESP_SOLICITOR_NAME, RESP_SOL_NAME);
        caseDetails.getData().put(RESP_SOLICITOR_EMAIL, RESP_SOL_EMAIL);
        caseDetails.getData().put(NOC_PARTY, RESPONDENT);

        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(caseDetails, false)).thenReturn(true);
        final ChangedRepresentative removedSolicitor = removedSolicitorService.getRemovedSolicitorAsCaseworker(caseDetails,
            false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(caseDetails);
        verify(caseDataService).isLitigantRepresented(caseDetails, false);

        assertEquals(RESP_SOL_NAME, removedSolicitor.getName());
        assertEquals(RESP_SOL_EMAIL, removedSolicitor.getEmail());
        assertNull(removedSolicitor.getOrganisation());
    }

    @Test
    void givenApplicantForRepresentationChange_whenApplicantWasRepresented_thenReturnChangedRepresentative() {
        final Organisation removedOrg = Organisation.builder().organisationID("REMOVED_ORG").build();

        FinremCaseData originalFinremCaseData = spy(FinremCaseData.class);
        when(originalFinremCaseData.getAppSolicitorName()).thenReturn(APP_SOL_NAME);
        when(originalFinremCaseData.getAppSolicitorEmail()).thenReturn(APP_SOL_EMAIL);
        originalFinremCaseData.setApplicantOrganisationPolicy(OrganisationPolicy.builder().organisation(removedOrg).build());

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setContactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(NoticeOfChangeParty.APPLICANT).build());

        when(caseDataService.isLitigantRepresented(originalFinremCaseData, true)).thenReturn(true);

        assertThat(removedSolicitorService.getChangedRepresentative(finremCaseData, originalFinremCaseData))
            .extracting(
                ChangedRepresentative::getName,
                ChangedRepresentative::getEmail,
                ChangedRepresentative::getOrganisation)
            .containsExactly(APP_SOL_NAME, APP_SOL_EMAIL, removedOrg);
    }

    @Test
    void givenRespondentForRepresentationChange_whenRespondentWasRepresented_thenReturnChangedRepresentative() {
        final Organisation removedOrg = Organisation.builder().organisationID("REMOVED_ORG").build();

        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getRespondentSolicitorEmail()).thenReturn(RESP_SOL_EMAIL);
        when(contactDetailsWrapper.getRespondentSolicitorName()).thenReturn(RESP_SOLICITOR_NAME);
        FinremCaseData originalFinremCaseData = spy(FinremCaseData.class);
        when(originalFinremCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        originalFinremCaseData.setRespondentOrganisationPolicy(OrganisationPolicy.builder().organisation(removedOrg).build());

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setContactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(NoticeOfChangeParty.RESPONDENT).build());

        when(caseDataService.isLitigantRepresented(originalFinremCaseData, false)).thenReturn(true);

        assertThat(removedSolicitorService.getChangedRepresentative(finremCaseData, originalFinremCaseData))
            .extracting(
                ChangedRepresentative::getName,
                ChangedRepresentative::getEmail,
                ChangedRepresentative::getOrganisation)
            .containsExactly(RESP_SOLICITOR_NAME, RESP_SOL_EMAIL, removedOrg);
    }

    @Test
    void givenApplicantForRepresentationChange_whenApplicantWasNotRepresented_thenReturnNull() {
        Organisation removedOrg = Organisation.builder().organisationID("REMOVED_ORG").build();

        FinremCaseData originalFinremCaseData = spy(FinremCaseData.class);
        originalFinremCaseData.setApplicantOrganisationPolicy(OrganisationPolicy.builder().organisation(removedOrg).build());

        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setContactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(NoticeOfChangeParty.APPLICANT).build());

        when(caseDataService.isLitigantRepresented(originalFinremCaseData, true)).thenReturn(false);

        assertThat(removedSolicitorService.getChangedRepresentative(finremCaseData, originalFinremCaseData))
            .isNull();
    }

    @Test
    void givenNocPartyMissing_whenGetChangedRepresentative_thenReturnNull() {
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        finremCaseData.setContactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(null).build());

        assertThat(removedSolicitorService.getChangedRepresentative(finremCaseData, mock(FinremCaseData.class))).isNull();
    }

    @Test
    void givenNoLitigantRepresented_whenGetChangedRepresentative_thenReturnNull() {
        FinremCaseData originalFinremCaseData = mock(FinremCaseData.class);
        FinremCaseData finremCaseData = spy(FinremCaseData.class);
        when(finremCaseData.getCcdCaseId()).thenReturn(CASE_ID);
        finremCaseData.setContactDetailsWrapper(ContactDetailsWrapper.builder().nocParty(NoticeOfChangeParty.APPLICANT).build());

        assertThat(removedSolicitorService.getChangedRepresentative(finremCaseData, originalFinremCaseData)).isNull();
        assertThat(logs.getInfos()).contains("1234567890 - No applicant or respondent organisation policy provided");
    }

    @TestLogs
    private final TestLogger logs = new TestLogger(RemovedSolicitorService.class);

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
