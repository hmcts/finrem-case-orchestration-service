package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentSendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_NOT_APPROVED_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ConsentSendOrderCorresponderTest {

    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;
    @Mock
    private ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;

    @InjectMocks
    private ConsentSendOrderCorresponder corresponder;

    @Test
    void givenCoverLetters_whenBuildEvents_thenEachPartyGetsTheirCoverLetterThenOrders() {
        CaseDocument applicantCoverLetter = document("appCoverLetter.pdf");
        CaseDocument respondentCoverLetter = document("respCoverLetter.pdf");
        CaseDocument order = document("order.pdf");
        NotificationRequest applicantRequest = NotificationRequest.builder().name("applicant").build();
        NotificationRequest respondentRequest = NotificationRequest.builder().name("respondent").build();

        FinremCaseDetails caseDetails = caseDetails(applicantCoverLetter, respondentCoverLetter);
        FinremCaseDetails caseDetailsBefore = caseDetails(null, null);

        when(consentOrderPrintService.getApprovedOrderDocumentsToSend(caseDetails, caseDetailsBefore, AUTH_TOKEN))
            .thenReturn(List.of(order));
        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)).thenReturn(applicantRequest);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)).thenReturn(respondentRequest);

        List<SendCorrespondenceEvent> events = corresponder.buildApprovedOrderEvents(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        assertEquals(2, events.size());

        SendCorrespondenceEvent applicantEvent = events.get(0);
        assertEquals(List.of(APPLICANT), applicantEvent.getNotificationParties());
        assertEquals(applicantRequest, applicantEvent.getEmailNotificationRequest());
        assertEquals(FR_CONSENT_ORDER_AVAILABLE, applicantEvent.getEmailTemplate());
        assertEquals(List.of(applicantCoverLetter, order), applicantEvent.getDocumentsToPost());
        assertEquals(caseDetails, applicantEvent.getCaseDetails());
        assertEquals(AUTH_TOKEN, applicantEvent.getAuthToken());

        SendCorrespondenceEvent respondentEvent = events.get(1);
        assertEquals(List.of(RESPONDENT), respondentEvent.getNotificationParties());
        assertEquals(respondentRequest, respondentEvent.getEmailNotificationRequest());
        assertEquals(FR_CONSENT_ORDER_AVAILABLE, respondentEvent.getEmailTemplate());
        assertEquals(List.of(respondentCoverLetter, order), respondentEvent.getDocumentsToPost());
        assertEquals(caseDetails, respondentEvent.getCaseDetails());
        assertEquals(AUTH_TOKEN, respondentEvent.getAuthToken());
    }

    @Test
    void givenNoCoverLetters_whenBuildEvents_thenBothPartiesGetOnlyOrders() {
        CaseDocument order = document("order.pdf");
        FinremCaseDetails caseDetails = caseDetails(null, null);
        FinremCaseDetails caseDetailsBefore = caseDetails(null, null);

        when(consentOrderPrintService.getApprovedOrderDocumentsToSend(caseDetails, caseDetailsBefore, AUTH_TOKEN))
            .thenReturn(List.of(order));

        List<SendCorrespondenceEvent> events = corresponder.buildApprovedOrderEvents(caseDetails, caseDetailsBefore, AUTH_TOKEN);

        assertEquals(List.of(order), events.get(0).getDocumentsToPost());
        assertEquals(List.of(order), events.get(1).getDocumentsToPost());
    }

    @Test
    void givenNotApprovedCoverLettersAndOrder_whenBuildNotApprovedEvents_thenEachPartyGetsCoverLetterThenOrder() {
        CaseDocument applicantCoverLetter = document("appCoverLetter.pdf");
        CaseDocument respondentCoverLetter = document("respCoverLetter.pdf");
        CaseDocument order = document("order.pdf");
        NotificationRequest applicantRequest = NotificationRequest.builder().name("applicant").build();
        NotificationRequest respondentRequest = NotificationRequest.builder().name("respondent").build();
        FinremCaseDetails caseDetails = notApprovedCaseDetails(applicantCoverLetter, respondentCoverLetter);

        when(consentOrderNotApprovedDocumentService.getNotApprovedConsentOrders(caseDetails, AUTH_TOKEN)).thenReturn(List.of(order));
        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)).thenReturn(applicantRequest);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)).thenReturn(respondentRequest);

        List<SendCorrespondenceEvent> events = corresponder.buildNotApprovedOrderEvents(caseDetails, AUTH_TOKEN);

        assertEquals(2, events.size());

        SendCorrespondenceEvent applicantEvent = events.get(0);
        assertEquals(List.of(APPLICANT), applicantEvent.getNotificationParties());
        assertEquals(applicantRequest, applicantEvent.getEmailNotificationRequest());
        assertEquals(FR_CONSENT_ORDER_NOT_APPROVED_SENT, applicantEvent.getEmailTemplate());
        assertEquals(List.of(applicantCoverLetter, order), applicantEvent.getDocumentsToPost());

        SendCorrespondenceEvent respondentEvent = events.get(1);
        assertEquals(List.of(RESPONDENT), respondentEvent.getNotificationParties());
        assertEquals(respondentRequest, respondentEvent.getEmailNotificationRequest());
        assertEquals(FR_CONSENT_ORDER_NOT_APPROVED_SENT, respondentEvent.getEmailTemplate());
        assertEquals(List.of(respondentCoverLetter, order), respondentEvent.getDocumentsToPost());
    }

    @Test
    void givenPaperApplicantDigitalRespondentAndNoOrders_whenBuildNotApprovedEvents_thenOnlyRespondentEventBuilt() {
        FinremCaseDetails caseDetails = notApprovedCaseDetails(document("appCoverLetter.pdf"), null);

        when(consentOrderNotApprovedDocumentService.getNotApprovedConsentOrders(caseDetails, AUTH_TOKEN)).thenReturn(List.of());

        List<SendCorrespondenceEvent> events = corresponder.buildNotApprovedOrderEvents(caseDetails, AUTH_TOKEN);

        assertEquals(1, events.size());
        assertEquals(List.of(RESPONDENT), events.get(0).getNotificationParties());
        assertEquals(List.of(), events.get(0).getDocumentsToPost());
    }

    private FinremCaseDetails caseDetails(CaseDocument applicantCoverLetter, CaseDocument respondentCoverLetter) {
        FinremCaseData caseData = FinremCaseData.builder()
            .consentSendOrderWrapper(ConsentSendOrderWrapper.builder()
                .consentOrderApprovedCoverLetterApp(applicantCoverLetter)
                .consentOrderApprovedCoverLetterResp(respondentCoverLetter)
                .build())
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
    }

    private CaseDocument document(String filename) {
        return CaseDocument.builder().documentFilename(filename).build();
    }

    private FinremCaseDetails notApprovedCaseDetails(CaseDocument applicantCoverLetter, CaseDocument respondentCoverLetter) {
        FinremCaseData caseData = FinremCaseData.builder()
            .consentSendOrderWrapper(ConsentSendOrderWrapper.builder()
                .consentOrderNotApprovedCoverLetterApp(applicantCoverLetter)
                .consentOrderNotApprovedCoverLetterResp(respondentCoverLetter)
                .build())
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
    }
}
