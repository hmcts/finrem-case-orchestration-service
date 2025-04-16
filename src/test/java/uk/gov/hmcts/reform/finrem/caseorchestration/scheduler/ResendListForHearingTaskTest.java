package uk.gov.hmcts.reform.finrem.caseorchestration.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ListForHearingWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CASE_CRON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class ResendListForHearingTaskTest {
    private ResendListForHearingTask resendListForHearingTask;
    @Mock
    private CcdService ccdService;
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private HearingDocumentService hearingDocumentService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @Captor
    private ArgumentCaptor<FinremCaseDetails> finremCaseDetailsArgumentCaptor;

    private static final String REFERENCE = "1234567890123456";
    private static final boolean APPLICANT_REPRESENTED = true;
    private static final boolean APPLICANT_NOT_REPRESENTED = false;
    private static final boolean RESPONDENT_REPRESENTED = true;
    private static final boolean RESPONDENT_NOT_REPRESENTED = false;
    private static final boolean FORM_C_EXISTS = true;
    private static final boolean FORM_C_NOT_EXISTS = false;

    @BeforeEach
    void setup() {
        resendListForHearingTask = new ResendListForHearingTask(caseReferenceCsvLoader, ccdService, systemUserService,
            finremCaseDetailsMapper, notificationService, hearingDocumentService);
        ReflectionTestUtils.setField(resendListForHearingTask, "taskEnabled", true);
        ReflectionTestUtils.setField(resendListForHearingTask, "csvFile", "test.csv");
        ReflectionTestUtils.setField(resendListForHearingTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(resendListForHearingTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoResend() {
        ReflectionTestUtils.setField(resendListForHearingTask, "taskEnabled", false);
        resendListForHearingTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(notificationService);
        verifyNoInteractions(hearingDocumentService);
    }

    @Test
    void givenApplicantOnlyRepresented_whenTaskRun_thenResendToRespondentOnly() {
        mockApplicantSolicitorDigital(true);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_NOT_REPRESENTED, FORM_C_EXISTS);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendListForHearingTask.run();

        verifyCorrespondenceSent(false, true);
        verifyCcdEvent("Applicant correspondence sent: false. Respondent correspondence sent: true");
    }

    @Test
    void givenApplicantOnlyRepresentedAndNotDigital_whenTaskRun_thenResendToBoth() {
        mockApplicantSolicitorDigital(false);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_NOT_REPRESENTED, FORM_C_EXISTS);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendListForHearingTask.run();

        verifyCorrespondenceSent(true, true);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: true");
    }

    @Test
    void givenRespondentOnlyRepresented_whenTaskRun_thenResendToApplicantOnly() {
        mockRespondentSolicitorDigital(true);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_NOT_REPRESENTED, RESPONDENT_REPRESENTED, FORM_C_EXISTS);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendListForHearingTask.run();

        verifyCorrespondenceSent(true, false);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: false");
    }

    @Test
    void givenRespondentOnlyRepresentedAndNotDigital_whenTaskRun_thenResendToBoth() {
        mockRespondentSolicitorDigital(false);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_NOT_REPRESENTED, RESPONDENT_REPRESENTED, FORM_C_EXISTS);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendListForHearingTask.run();

        verifyCorrespondenceSent(true, true);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: true");
    }

    @Test
    void givenApplicantAndRespondentRepresentedAndSolicitorsNotDigital_whenTaskRun_thenResendToBoth() {
        mockApplicantSolicitorDigital(false);
        mockRespondentSolicitorDigital(false);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_REPRESENTED, FORM_C_EXISTS);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendListForHearingTask.run();

        verifyCorrespondenceSent(true, true);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: true");
    }

    @Test
    void givenApplicantAndRespondentRepresentedAndSolicitorsDigital_whenTaskRun_thenNoResend() {
        mockApplicantSolicitorDigital(true);
        mockRespondentSolicitorDigital(true);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_REPRESENTED, FORM_C_EXISTS);
        mockSearchCases(caseDetails);

        resendListForHearingTask.run();

        verify(ccdService, never()).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verifyNoInteractions(hearingDocumentService);
    }

    @Test
    void givenNoFormC_whenTaskRun_thenNoResend() {
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        CaseDetails caseDetails = createCaseData(APPLICANT_NOT_REPRESENTED, RESPONDENT_NOT_REPRESENTED, FORM_C_NOT_EXISTS);
        mockSearchCases(caseDetails);

        resendListForHearingTask.run();

        verify(ccdService, never()).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verifyNoInteractions(hearingDocumentService);
    }

    private void mockLoadCaseReferenceList() {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(REFERENCE);
        when(caseReferenceCsvLoader.loadCaseReferenceList("test.csv", "DUMMY_SECRET"))
            .thenReturn(List.of(caseReference));
    }

    private void mockSystemUserToken() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
    }

    private void mockSearchCases(CaseDetails caseDetails) {
        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();
        when(ccdService.getCaseByCaseId(REFERENCE, CaseType.CONTESTED, AUTH_TOKEN)).thenReturn(searchResult);
    }

    private CaseDetails createCaseData(boolean applicantRepresented, boolean respondentRepresented, boolean formCExists) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.forValue(applicantRepresented))
                .contestedRespondentRepresented(YesOrNo.forValue(respondentRepresented))
                .build())
            .listForHearingWrapper(ListForHearingWrapper.builder()
                .formC(formCExists ? new CaseDocument() : null)
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.READY_FOR_HEARING)
            .data(caseData)
            .build();

        return finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
    }

    private void mockStartEvent(CaseDetails caseDetails) {
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(ccdService.startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType())).thenReturn(startEventResponse);
    }

    private void mockApplicantSolicitorDigital(boolean applicantSolicitorDigital) {
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(applicantSolicitorDigital);
    }

    private void mockRespondentSolicitorDigital(boolean respondentSolicitorDigital) {
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(any(FinremCaseDetails.class)))
            .thenReturn(respondentSolicitorDigital);
    }

    private void verifyCorrespondenceSent(boolean applicantCorrespondenceSent, boolean respondentCorrespondentSent) {
        verify(hearingDocumentService, times(1)).sendInitialHearingCorrespondence(finremCaseDetailsArgumentCaptor.capture(),
            eq(AUTH_TOKEN));
        verifyNoMoreInteractions(hearingDocumentService);
        FinremCaseData caseData = finremCaseDetailsArgumentCaptor.getValue().getData();
        assertThat(caseData.isApplicantCorrespondenceEnabled()).isEqualTo(applicantCorrespondenceSent);
        assertThat(caseData.isRespondentCorrespondenceEnabled()).isEqualTo(respondentCorrespondentSent);
    }

    private void verifyCcdEvent(String eventDescription) {
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verify(ccdService).submitEventForCaseWorker(any(StartEventResponse.class), eq(AUTH_TOKEN), eq(REFERENCE),
            eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
            eq("DFR-3710 - Resend List for Hearing"),
            eq(eventDescription));
    }
}
