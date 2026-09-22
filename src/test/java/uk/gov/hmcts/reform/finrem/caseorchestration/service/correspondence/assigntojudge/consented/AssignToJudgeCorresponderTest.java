package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.consented;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest.FinremNotificationRequestMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignedToJudgeDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationAuditService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckSolicitorIsDigitalService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_ASSIGNED_TO_JUDGE;

@ExtendWith(MockitoExtension.class)
class AssignToJudgeCorresponderTest {

    @Mock
    private AssignedToJudgeDocumentService assignedToJudgeDocumentService;

    @Mock
    private FinremNotificationRequestMapper finremNotificationRequestMapper;

    @Mock
    private CheckSolicitorIsDigitalService checkSolicitorIsDigitalService;

    @Mock
    private NotificationAuditService notificationAuditService;

    @InjectMocks
    private AssignToJudgeCorresponder assignToJudgeCorresponder;

    @Captor
    ArgumentCaptor<SendCorrespondenceEvent> sendCorrespondenceEventArgumentCaptor;

    @Test
    void shouldCreateAuditsForCorrespondence() {
        // Arrange
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getId()).thenReturn(CASE_ID_IN_LONG);

        EventType eventType = EventType.ISSUE_APPLICATION;

        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID)).thenReturn(true);
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID)).thenReturn(true);

        NotificationRequest applicantNotificationRequest = mock(NotificationRequest.class);
        when(finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(finremCaseDetails, false)
        ).thenReturn(applicantNotificationRequest);

        NotificationRequest respondentNotificationRequest = mock(NotificationRequest.class);
        when(finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(finremCaseDetails, false)
        ).thenReturn(respondentNotificationRequest);

        when(notificationAuditService
            .createAuditsForCorrespondence(any(SendCorrespondenceEvent.class), eq(eventType)))
            .thenReturn("tracker-valid-id");

        // Act
        assignToJudgeCorresponder.createAuditsForCorrespondence(eventType, finremCaseDetails, AUTH_TOKEN);

        // Verify
        assertAll(
            () -> verify(notificationAuditService, times(2))
                .createAuditsForCorrespondence(sendCorrespondenceEventArgumentCaptor.capture(), eq(eventType)),
            () -> verifyNoInteractions(assignedToJudgeDocumentService),
            () -> {
                List<SendCorrespondenceEvent> events = sendCorrespondenceEventArgumentCaptor.getAllValues();

                assertAll(
                    // Applicant event
                    () -> assertEquals(eventType.name(), events.getFirst().getEventId()),
                    () -> assertEquals(finremCaseDetails, events.getFirst().getCaseDetails()),
                    () -> assertEquals(
                        List.of(NotificationParty.APPLICANT),
                        events.getFirst().getNotificationParties()
                    ),
                    () -> assertEquals(FR_ASSIGNED_TO_JUDGE, events.getFirst().getEmailTemplate()),
                    () -> assertEquals(
                        applicantNotificationRequest,
                        events.getFirst().getEmailNotificationRequest()
                    ),
                    () -> assertEquals(AUTH_TOKEN, events.getFirst().getAuthToken()),
                    () -> assertThat(events.getFirst().getDocumentsToPost()).isEmpty(),

                    // Respondent event
                    () -> assertEquals(eventType.name(), events.get(1).getEventId()),
                    () -> assertEquals(finremCaseDetails, events.get(1).getCaseDetails()),
                    () -> assertEquals(
                        List.of(NotificationParty.RESPONDENT),
                        events.get(1).getNotificationParties()
                    ),
                    () -> assertEquals(FR_ASSIGNED_TO_JUDGE, events.get(1).getEmailTemplate()),
                    () -> assertEquals(
                        respondentNotificationRequest,
                        events.get(1).getEmailNotificationRequest()
                    ),
                    () -> assertEquals(AUTH_TOKEN, events.get(1).getAuthToken()),
                    () -> assertEquals("tracker-valid-id", // respondent event should be the first tracker id
                        events.get(1).getNotificationTrackerId()),
                    () -> assertThat(events.get(1).getDocumentsToPost()).isEmpty()
                );
            }
        );
    }

    @Test
    void shouldGenerateDocument_whenBuildSendCorrespondenceEvents() {
        // Arrange
        FinremCaseDetails finremCaseDetails = mock(FinremCaseDetails.class);
        when(finremCaseDetails.getId()).thenReturn(CASE_ID_IN_LONG);

        EventType eventType = EventType.ISSUE_APPLICATION;

        when(checkSolicitorIsDigitalService.isApplicantSolicitorDigital(CASE_ID)).thenReturn(true);
        when(checkSolicitorIsDigitalService.isRespondentSolicitorDigital(CASE_ID)).thenReturn(true);

        NotificationRequest applicantNotificationRequest = mock(NotificationRequest.class);
        when(finremNotificationRequestMapper
            .getNotificationRequestForApplicantSolicitor(finremCaseDetails, false)
        ).thenReturn(applicantNotificationRequest);

        NotificationRequest respondentNotificationRequest = mock(NotificationRequest.class);
        when(finremNotificationRequestMapper
            .getNotificationRequestForRespondentSolicitor(finremCaseDetails, false)
        ).thenReturn(respondentNotificationRequest);

        CaseDocument applicantDoc = caseDocument("applicantDoc");
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(finremCaseDetails, AUTH_TOKEN,
                DocumentHelper.PaperNotificationRecipient.APPLICANT)).thenReturn(applicantDoc);
        CaseDocument respondentDoc = caseDocument("respondentDoc");
        when(assignedToJudgeDocumentService.generateAssignedToJudgeNotificationLetter(finremCaseDetails, AUTH_TOKEN,
            DocumentHelper.PaperNotificationRecipient.RESPONDENT)).thenReturn(respondentDoc);

        // Act
        List<SendCorrespondenceEvent> events = assignToJudgeCorresponder.buildSendCorrespondenceEvents(eventType, finremCaseDetails, AUTH_TOKEN);

        // Verify
        assertAll(
            () -> verifyNoInteractions(notificationAuditService),
            // Applicant event
            () -> assertEquals(eventType.name(), events.getFirst().getEventId()),
            () -> assertEquals(finremCaseDetails, events.getFirst().getCaseDetails()),
            () -> assertEquals(
                List.of(NotificationParty.APPLICANT),
                events.getFirst().getNotificationParties()
            ),
            () -> assertEquals(FR_ASSIGNED_TO_JUDGE, events.getFirst().getEmailTemplate()),
            () -> assertEquals(
                applicantNotificationRequest,
                events.getFirst().getEmailNotificationRequest()
            ),
            () -> assertEquals(AUTH_TOKEN, events.getFirst().getAuthToken()),
            () -> assertNull(events.getFirst().getNotificationTrackerId()),
            () -> assertThat(events.getFirst().getDocumentsToPost()).containsOnly(applicantDoc),

            // Respondent event
            () -> assertEquals(eventType.name(), events.get(1).getEventId()),
            () -> assertEquals(finremCaseDetails, events.get(1).getCaseDetails()),
            () -> assertEquals(
                List.of(NotificationParty.RESPONDENT),
                events.get(1).getNotificationParties()
            ),
            () -> assertEquals(FR_ASSIGNED_TO_JUDGE, events.get(1).getEmailTemplate()),
            () -> assertEquals(
                respondentNotificationRequest,
                events.get(1).getEmailNotificationRequest()
            ),
            () -> assertEquals(AUTH_TOKEN, events.get(1).getAuthToken()),
            () -> assertNull(events.get(1).getNotificationTrackerId()),
            () -> assertThat(events.get(1).getDocumentsToPost()).containsOnly(respondentDoc)
        );
    }
}
