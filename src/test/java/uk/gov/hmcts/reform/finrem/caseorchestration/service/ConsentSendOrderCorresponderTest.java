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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ConsentSendOrderCorresponderTest {

    @Mock
    private ConsentOrderPrintService consentOrderPrintService;
    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;

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

    private FinremCaseDetails caseDetails(CaseDocument applicantCoverLetter, CaseDocument respondentCoverLetter) {
        FinremCaseData caseData = FinremCaseData.builder()
            .consentOrderApprovedCoverLetterApp(applicantCoverLetter)
            .consentOrderApprovedCoverLetterResp(respondentCoverLetter)
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
    }

    private CaseDocument document(String filename) {
        return CaseDocument.builder().documentFilename(filename).build();
    }
}