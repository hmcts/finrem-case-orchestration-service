package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONSENT_ORDER_AVAILABLE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty.RESPONDENT;

@ExtendWith(MockitoExtension.class)
class ConsentOrderApprovedCorresponderTest {

    private static final String AUTH_TOKEN = "authToken";

    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationRequest applicantRequest;
    @Mock
    private NotificationRequest respondentRequest;

    @InjectMocks
    private ConsentOrderApprovedCorresponder corresponder;

    @Test
    void givenBothSolicitorsDigital_whenBuildEvents_thenEmailEventForEachPartyWithNoDocuments() {
        FinremCaseDetails caseDetails = caseDetails(null, null);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)).thenReturn(applicantRequest);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)).thenReturn(respondentRequest);

        List<SendCorrespondenceEvent> events = corresponder.buildCorrespondenceEvents(caseDetails, AUTH_TOKEN);

        assertEquals(2, events.size());
        assertEvent(events.get(0), APPLICANT, applicantRequest, caseDetails);
        assertEvent(events.get(1), RESPONDENT, respondentRequest, caseDetails);
        assertTrue(events.get(0).getDocumentsToPost().isEmpty());
        assertTrue(events.get(1).getDocumentsToPost().isEmpty());
    }

    @Test
    void givenPaperPartiesWithPacks_whenBuildEvents_thenEventsCarryEachPackInOrder() {
        List<DocumentCollectionItem> applicantPack = pack("appCoverLetter.pdf", "order.pdf");
        List<DocumentCollectionItem> respondentPack = pack("order.pdf", "resCoverLetter.pdf");
        FinremCaseDetails caseDetails = caseDetails(applicantPack, respondentPack);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)).thenReturn(applicantRequest);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)).thenReturn(respondentRequest);

        List<SendCorrespondenceEvent> events = corresponder.buildCorrespondenceEvents(caseDetails, AUTH_TOKEN);

        assertEquals(2, events.size());
        assertEvent(events.get(0), APPLICANT, applicantRequest, caseDetails);
        assertEvent(events.get(1), RESPONDENT, respondentRequest, caseDetails);
        assertEquals(List.of("appCoverLetter.pdf", "order.pdf"), filenames(events.get(0)));
        assertEquals(List.of("order.pdf", "resCoverLetter.pdf"), filenames(events.get(1)));
    }

    @Test
    void givenPaperApplicantWithEmptyPack_whenBuildEvents_thenNoApplicantEvent() {
        FinremCaseDetails caseDetails = caseDetails(null, null);
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(finremNotificationRequestMapper.getNotificationRequestForRespondentSolicitor(caseDetails)).thenReturn(respondentRequest);

        List<SendCorrespondenceEvent> events = corresponder.buildCorrespondenceEvents(caseDetails, AUTH_TOKEN);

        assertEquals(1, events.size());
        assertEvent(events.getFirst(), RESPONDENT, respondentRequest, caseDetails);
        verify(finremNotificationRequestMapper, never()).getNotificationRequestForApplicantSolicitor(any());
    }

    @Test
    void givenPaperRespondentWithEmptyPack_whenBuildEvents_thenNoRespondentEvent() {
        FinremCaseDetails caseDetails = caseDetails(null, List.of());
        when(notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(true);
        when(notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)).thenReturn(false);
        when(finremNotificationRequestMapper.getNotificationRequestForApplicantSolicitor(caseDetails)).thenReturn(applicantRequest);

        List<SendCorrespondenceEvent> events = corresponder.buildCorrespondenceEvents(caseDetails, AUTH_TOKEN);

        assertEquals(1, events.size());
        assertEvent(events.getFirst(), APPLICANT, applicantRequest, caseDetails);
        verify(finremNotificationRequestMapper, never()).getNotificationRequestForRespondentSolicitor(any());
    }

    private void assertEvent(SendCorrespondenceEvent event, Object party, NotificationRequest request, FinremCaseDetails caseDetails) {
        assertEquals(List.of(party), event.getNotificationParties());
        assertEquals(FR_CONSENT_ORDER_AVAILABLE, event.getEmailTemplate());
        assertEquals(request, event.getEmailNotificationRequest());
        assertEquals(caseDetails, event.getCaseDetails());
        assertEquals(AUTH_TOKEN, event.getAuthToken());
    }

    private FinremCaseDetails caseDetails(List<DocumentCollectionItem> applicantPack, List<DocumentCollectionItem> respondentPack) {
        FinremCaseData caseData = FinremCaseData.builder()
            .consentOrderApplicantLetterPack(applicantPack)
            .consentOrderRespondentLetterPack(respondentPack)
            .build();
        return FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
    }

    private List<DocumentCollectionItem> pack(String... filenames) {
        return java.util.Arrays.stream(filenames)
            .map(name -> DocumentCollectionItem.fromCaseDocument(CaseDocument.builder().documentFilename(name).build()))
            .toList();
    }

    private List<String> filenames(SendCorrespondenceEvent event) {
        return event.getDocumentsToPost().stream().map(CaseDocument::getDocumentFilename).toList();
    }
}
