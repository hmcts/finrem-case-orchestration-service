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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.reset;
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

        reset(caseDataService, checkApplicantSolicitorIsDigitalService);

        FinremCaseData finremCaseData  = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .ccdCaseId(CASE_ID)
            .applicantOrganisationPolicy(getApplicantOrgPolicyWithOrganisation())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .applicantSolicitorName(APP_SOL_NAME)
                .applicantSolicitorEmail(APP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .build())
            .build();

        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(true);
        ChangedRepresentative removedSolicitor2 = removedSolicitorService.getRemovedSolicitorAsCaseworker(finremCaseData,
            true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, true);

        Stream.of(removedSolicitor, removedSolicitor2).forEach(result -> {
            assertEquals(APP_SOL_NAME, result.getName());
            assertEquals(APP_SOL_EMAIL, result.getEmail());
            assertEquals(TEST_APP_ORG_ID, result.getOrganisation().getOrganisationID());
            assertEquals(TEST_APP_ORG_NAME, result.getOrganisation().getOrganisationName());
        });
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

        reset(caseDataService, checkApplicantSolicitorIsDigitalService);

        FinremCaseData finremCaseData  = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .ccdCaseId(CASE_ID)
            .applicantOrganisationPolicy(getApplicantOrgPolicyWithOrganisation())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .solicitorName(APP_SOL_NAME)
                .solicitorEmail(APP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .build())
            .build();
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(true);

        final ChangedRepresentative removedSolicitor2 = removedSolicitorService.getRemovedSolicitorAsCaseworker(finremCaseData,
            true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, true);

        Stream.of(removedSolicitor, removedSolicitor2).forEach(result -> {
            assertEquals(APP_SOL_NAME, result.getName());
            assertEquals(APP_SOL_EMAIL, result.getEmail());
            assertEquals(TEST_APP_ORG_ID, result.getOrganisation().getOrganisationID());
            assertEquals(TEST_APP_ORG_NAME, result.getOrganisation().getOrganisationName());
        });
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

        reset(caseDataService, checkApplicantSolicitorIsDigitalService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .ccdCaseId(CASE_ID)
            .applicantOrganisationPolicy(getApplicantOrgPolicyWithoutOrganisation())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.YES)
                .solicitorName(APP_SOL_NAME)
                .solicitorEmail(APP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.APPLICANT)
                .build())
            .build();
        when(checkApplicantSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(finremCaseData, true)).thenReturn(true);
        ChangedRepresentative removedSolicitor2 = removedSolicitorService.getRemovedSolicitorAsCaseworker(finremCaseData,
            true);
        verify(checkApplicantSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, true);

        Stream.of(removedSolicitor, removedSolicitor2).forEach(result -> {
            assertEquals(APP_SOL_NAME, result.getName());
            assertEquals(APP_SOL_EMAIL, result.getEmail());
            assertNull(result.getOrganisation());
        });
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

        reset(caseDataService, checkRespondentSolicitorIsDigitalService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .ccdCaseId(CASE_ID)
            .respondentOrganisationPolicy(getRespondentOrgPolicyWithOrganisation())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .contestedRespondentRepresented(YesOrNo.YES)
                .respondentSolicitorName(RESP_SOL_NAME)
                .respondentSolicitorEmail(RESP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .build())
            .build();
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(finremCaseData, false)).thenReturn(true);
        ChangedRepresentative removedSolicitor2 = removedSolicitorService.getRemovedSolicitorAsCaseworker(finremCaseData,
            false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, false);

        Stream.of(removedSolicitor, removedSolicitor2).forEach(result -> {
            assertEquals(RESP_SOL_NAME, result.getName());
            assertEquals(RESP_SOL_EMAIL, result.getEmail());
            assertEquals(TEST_RESP_ORG_ID, result.getOrganisation().getOrganisationID());
            assertEquals(TEST_RESP_ORG_NAME, result.getOrganisation().getOrganisationName());
        });
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

        reset(caseDataService, checkRespondentSolicitorIsDigitalService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .ccdCaseId(CASE_ID)
            .respondentOrganisationPolicy(getRespondentOrgPolicyWithOrganisation())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .respondentSolicitorName(RESP_SOL_NAME)
                .respondentSolicitorEmail(RESP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .build())
            .build();
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(true);
        when(caseDataService.isLitigantRepresented(finremCaseData, false)).thenReturn(true);
        ChangedRepresentative removedSolicitor2 = removedSolicitorService.getRemovedSolicitorAsCaseworker(finremCaseData,
            false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, false);

        Stream.of(removedSolicitor, removedSolicitor2).forEach(result -> {
            assertEquals(RESP_SOL_NAME, result.getName());
            assertEquals(RESP_SOL_EMAIL, result.getEmail());
            assertEquals(TEST_RESP_ORG_ID, result.getOrganisation().getOrganisationID());
            assertEquals(TEST_RESP_ORG_NAME, result.getOrganisation().getOrganisationName());
        });
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

        reset(caseDataService, checkRespondentSolicitorIsDigitalService);

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONSENTED)
            .ccdCaseId(CASE_ID)
            .respondentOrganisationPolicy(getRespondentOrgPolicyWithOrganisation())
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .consentedRespondentRepresented(YesOrNo.YES)
                .respondentSolicitorName(RESP_SOL_NAME)
                .respondentSolicitorEmail(RESP_SOL_EMAIL)
                .nocParty(NoticeOfChangeParty.RESPONDENT)
                .build())
            .build();
        when(checkRespondentSolicitorIsDigitalService.isSolicitorDigital(finremCaseData)).thenReturn(false);
        when(caseDataService.isLitigantRepresented(finremCaseData, false)).thenReturn(true);
        ChangedRepresentative removedSolicitor2 = removedSolicitorService.getRemovedSolicitorAsCaseworker(finremCaseData,
            false);
        verify(checkRespondentSolicitorIsDigitalService).isSolicitorDigital(finremCaseData);
        verify(caseDataService).isLitigantRepresented(finremCaseData, false);

        Stream.of(removedSolicitor, removedSolicitor2).forEach(result -> {
            assertEquals(RESP_SOL_NAME, result.getName());
            assertEquals(RESP_SOL_EMAIL, result.getEmail());
            assertNull(result.getOrganisation());
        });
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
