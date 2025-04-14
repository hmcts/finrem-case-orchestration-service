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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.State;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.InterimWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDocumentsHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CcdService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReference;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv.CaseReferenceCsvLoader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
class ResendInterimHearingTaskTest {
    private ResendInterimHearingTask resendInterimHearingTask;
    @Mock
    private CcdService ccdService;
    @Mock
    private CaseReferenceCsvLoader caseReferenceCsvLoader;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private InterimHearingService interimHearingService;
    @Mock
    private NotificationService notificationService;

    private final FinremCaseDetailsMapper finremCaseDetailsMapper = new FinremCaseDetailsMapper(
        new ObjectMapper().registerModule(new JavaTimeModule()));

    @Captor
    private ArgumentCaptor<FinremCaseDetails> finremCaseDetailsArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<InterimHearingCollection>> interimHearingsToSendCaptor;

    private static final String REFERENCE = "1234567890123456";
    private static final boolean APPLICANT_REPRESENTED = true;
    private static final boolean APPLICANT_NOT_REPRESENTED = false;
    private static final boolean RESPONDENT_REPRESENTED = true;
    private static final boolean RESPONDENT_NOT_REPRESENTED = false;


    @BeforeEach
    void setup() {
        resendInterimHearingTask = new ResendInterimHearingTask(caseReferenceCsvLoader, ccdService, systemUserService,
            finremCaseDetailsMapper, interimHearingService, notificationService);

        ReflectionTestUtils.setField(resendInterimHearingTask, "taskEnabled", true);
        ReflectionTestUtils.setField(resendInterimHearingTask, "csvFile", "test.csv");
        ReflectionTestUtils.setField(resendInterimHearingTask, "secret", "DUMMY_SECRET");
        ReflectionTestUtils.setField(resendInterimHearingTask, "caseTypeId", CaseType.CONTESTED.getCcdType());
    }

    @Test
    void givenTaskNotEnabled_whenTaskRun_thenNoResend() {
        ReflectionTestUtils.setField(resendInterimHearingTask, "taskEnabled", false);
        resendInterimHearingTask.run();

        verifyNoInteractions(ccdService);
        verifyNoInteractions(caseReferenceCsvLoader);
        verifyNoInteractions(systemUserService);
        verifyNoInteractions(interimHearingService);
    }

    @Test
    void givenApplicantOnlyRepresented_whenTaskRun_thenResendToRespondentOnly() {
        mockApplicantSolicitorDigital(true);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        FinremCaseDetails finremCaseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_NOT_REPRESENTED);
        CaseDocumentsHolder caseDocuments = createCaseDocumentsHolder();
        mockDocsToPrint(finremCaseDetails, caseDocuments);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendInterimHearingTask.run();

        verifyCorrespondenceSent(false, true, caseDocuments);
        verifyCcdEvent("Applicant correspondence sent: false. Respondent correspondence sent: true");
    }

    @Test
    void givenApplicantOnlyRepresentedAndNotDigital_whenTaskRun_thenResendToBoth() {
        mockApplicantSolicitorDigital(false);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        FinremCaseDetails finremCaseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_NOT_REPRESENTED);
        CaseDocumentsHolder caseDocuments = createCaseDocumentsHolder();
        mockDocsToPrint(finremCaseDetails, caseDocuments);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendInterimHearingTask.run();

        verifyCorrespondenceSent(true, true, caseDocuments);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: true");
    }

    @Test
    void givenRespondentOnlyRepresented_whenTaskRun_thenResendToApplicantOnly() {
        mockRespondentSolicitorDigital(true);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        FinremCaseDetails finremCaseDetails = createCaseData(APPLICANT_NOT_REPRESENTED, RESPONDENT_REPRESENTED);
        CaseDocumentsHolder caseDocuments = createCaseDocumentsHolder();
        mockDocsToPrint(finremCaseDetails, caseDocuments);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendInterimHearingTask.run();

        verifyCorrespondenceSent(true, false, caseDocuments);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: false");
    }

    @Test
    void givenRespondentOnlyRepresentedAndNotDigital_whenTaskRun_thenResendToBoth() {
        mockRespondentSolicitorDigital(false);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        FinremCaseDetails finremCaseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_REPRESENTED);
        CaseDocumentsHolder caseDocuments = createCaseDocumentsHolder();
        mockDocsToPrint(finremCaseDetails, caseDocuments);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendInterimHearingTask.run();

        verifyCorrespondenceSent(true, true, caseDocuments);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: true");
    }

    @Test
    void givenApplicantAndRespondentRepresentedAndSolicitorsNotDigital_whenTaskRun_thenResendToBoth() {
        mockApplicantSolicitorDigital(false);
        mockRespondentSolicitorDigital(false);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        FinremCaseDetails finremCaseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_REPRESENTED);
        CaseDocumentsHolder caseDocuments = createCaseDocumentsHolder();
        mockDocsToPrint(finremCaseDetails, caseDocuments);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        mockSearchCases(caseDetails);
        mockStartEvent(caseDetails);

        resendInterimHearingTask.run();

        verifyCorrespondenceSent(true, true, caseDocuments);
        verifyCcdEvent("Applicant correspondence sent: true. Respondent correspondence sent: true");
    }

    @Test
    void givenApplicantAndRespondentRepresentedAndSolicitorsDigital_whenTaskRun_thenNoResend() {
        mockApplicantSolicitorDigital(true);
        mockRespondentSolicitorDigital(true);
        mockLoadCaseReferenceList();
        mockSystemUserToken();
        FinremCaseDetails finremCaseDetails = createCaseData(APPLICANT_REPRESENTED, RESPONDENT_REPRESENTED);
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        mockSearchCases(caseDetails);

        resendInterimHearingTask.run();

        verify(ccdService, never()).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verifyNoInteractions(interimHearingService);
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

    private FinremCaseDetails createCaseData(boolean applicantRepresented, boolean respondentRepresented) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper.builder()
                .applicantRepresented(YesOrNo.forValue(applicantRepresented))
                .contestedRespondentRepresented(YesOrNo.forValue(respondentRepresented))
                .build())
            .interimWrapper(InterimWrapper.builder()
                .interimHearings(List.of(
                    InterimHearingCollection.builder()
                        .id(UUID.randomUUID())
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FH)
                            .interimHearingDate(LocalDate.of(2025, 5, 2))
                            .interimHearingTime("10:00")
                            .build())
                        .build(),
                    InterimHearingCollection.builder()
                        .id(UUID.randomUUID())
                        .value(InterimHearingItem.builder()
                            .interimHearingType(InterimTypeOfHearing.FH)
                            .interimHearingDate(LocalDate.of(2025, 2, 1))
                            .interimHearingTime("10:00")
                            .build())
                        .build()
                ))
                .interimHearingDocuments(List.of(
                    InterimHearingBulkPrintDocumentsData.builder()
                        .id("id2")
                        .value(InterimHearingBulkPrintDocument.builder()
                            .caseDocument(CaseDocument.builder()
                                .documentUrl("http://ExistingHearingNotice2.url")
                                .documentFilename("InterimHearingNotice.pdf")
                                .uploadTimestamp(LocalDateTime.of(2025, 3, 2, 0, 0))
                                .build())
                            .build())
                        .build(),
                    InterimHearingBulkPrintDocumentsData.builder()
                        .id("id1")
                        .value(InterimHearingBulkPrintDocument.builder()
                            .caseDocument(CaseDocument.builder()
                                .documentUrl("http://ExistingHearingNotice1.url")
                                .documentFilename("InterimHearingNotice.pdf")
                                .uploadTimestamp(LocalDateTime.of(2024, 12, 1, 0, 0))
                                .build())
                            .build())
                        .build()
                ))
                .build())
            .build();

        return FinremCaseDetails.builder()
            .id(Long.parseLong(REFERENCE))
            .caseType(CaseType.CONTESTED)
            .state(State.APPLICATION_SUBMITTED)
            .data(caseData)
            .build();
    }

    private CaseDocumentsHolder createCaseDocumentsHolder() {
        return CaseDocumentsHolder.builder()
            .caseDocuments(List.of(
                CaseDocument.builder()
                    .documentUrl("http://InterimHearingNotice3.url")
                    .uploadTimestamp(LocalDateTime.now())
                    .documentFilename("InterimHearingNotice.pdf")
                    .build()
            ))
            .bulkPrintDocuments(List.of(
                BulkPrintDocument.builder()
                    .binaryFileUrl("http://InterimHearingNotice3.url/binary")
                    .fileName("InterimHearingNotice.pdf")
                    .build()
            ))
            .build();
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

    private void mockDocsToPrint(FinremCaseDetails finremCaseDetails, CaseDocumentsHolder caseDocumentsToReturn) {
        when(interimHearingService.prepareDocumentsForPrint(
            any(FinremCaseDetails.class),
            any(),
            eq(AUTH_TOKEN)
        )).thenAnswer(invocation -> {
            FinremCaseDetails details = invocation.getArgument(0);
            // Mock the changes to finremCaseData
            details.getData().getInterimWrapper().getInterimHearingDocuments().add(
                InterimHearingBulkPrintDocumentsData.builder()
                    .id("id3")
                    .value(InterimHearingBulkPrintDocument.builder()
                        .caseDocument(CaseDocument.builder()
                            .documentUrl("http://InterimHearingNotice3.url")
                            .uploadTimestamp(LocalDateTime.now())
                            .documentFilename("InterimHearingNotice3.pdf")
                            .build())
                        .build())
                    .build()
            );
            return caseDocumentsToReturn; // Return the mocked CaseDocumentsHolder
        });
    }

    private void verifyCorrespondenceSent(boolean applicantCorrespondenceSent, boolean respondentCorrespondentSent, CaseDocumentsHolder documentsToPrint) {
        // TODO: Verify interim hearing content
        verify(interimHearingService, times(1)).prepareDocumentsForPrint(
            finremCaseDetailsArgumentCaptor.capture(),
            interimHearingsToSendCaptor.capture(),
            eq(AUTH_TOKEN)
        );
        verify(interimHearingService, times(1)).sendToBulkPrint(finremCaseDetailsArgumentCaptor.capture(), eq(AUTH_TOKEN), eq(documentsToPrint));
        verifyNoMoreInteractions(interimHearingService);

        List<InterimHearingCollection> interimHearingsToSend = interimHearingsToSendCaptor.getValue();
        FinremCaseData caseData = finremCaseDetailsArgumentCaptor.getValue().getData();

        // Assert on interim hearings to send list
        assertThat(interimHearingsToSend).hasSize(1);
        InterimHearingCollection hearingToSend = interimHearingsToSend.getFirst();
        assertThat(hearingToSend.getValue().getInterimHearingDate()).isEqualTo(LocalDate.of(2025, 5, 2));
        assertThat(hearingToSend.getValue().getInterimHearingTime()).isEqualTo("10:00");
        assertThat(hearingToSend.getValue().getInterimHearingType()).isEqualTo(InterimTypeOfHearing.FH);

        // Assert on Case Data
        assertThat(caseData.isApplicantCorrespondenceEnabled()).isEqualTo(applicantCorrespondenceSent);
        assertThat(caseData.isRespondentCorrespondenceEnabled()).isEqualTo(respondentCorrespondentSent);
        // Assert that new generated Interim Hearing document is deleted from case data
        assertThat(caseData.getInterimWrapper().getInterimHearingDocuments()).hasSize(2);
        assertThat(caseData.getInterimWrapper().getInterimHearingDocuments())
            .noneMatch(doc -> "id3".equals(doc.getId()));
    }

    private void verifyCcdEvent(String eventDescription) {
        verify(ccdService, times(1)).startEventForCaseWorker(AUTH_TOKEN, REFERENCE, CONTESTED.getCcdType(),
            AMEND_CASE_CRON.getCcdType());
        verify(ccdService).submitEventForCaseWorker(any(StartEventResponse.class), eq(AUTH_TOKEN), eq(REFERENCE),
            eq(CONTESTED.getCcdType()), eq(AMEND_CASE_CRON.getCcdType()),
            eq("DFR-3714 - Resend List for Interim Hearing"),
            eq(eventDescription));
    }
}
