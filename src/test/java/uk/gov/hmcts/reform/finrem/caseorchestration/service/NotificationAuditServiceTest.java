package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_AUDITS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_TO_BE_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;

@ExtendWith(MockitoExtension.class)
class NotificationAuditServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NotificationAuditService notificationAuditService;

    @Test
    void givenNotificationAuditCreated_whenCreateAuditsForCorrespondence_thenPendingNotificationIsCreated() {
        SendCorrespondenceEvent event = buildEvent();

        doAnswer(invocation -> {
            SendCorrespondenceEvent publishedEvent = invocation.getArgument(0);
            publishedEvent.recordEmailNotificationToSendAudit(NotificationParty.APPLICANT);
            return null;
        }).when(applicationEventPublisher).publishEvent(event);

        notificationAuditService.createAuditsForCorrespondence(event, EventType.MANAGE_HEARINGS);

        NotificationToBeSentCollectionItem pending =
            event.getCaseData().getNotificationAuditWrapper().getNotificationsToBeSent().getFirst();

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.isDryRun()).isTrue();
        assertThat(pending.getId()).isNotNull();
        assertThat(pending.getValue().getParty()).isEqualTo(NotificationParty.APPLICANT.name());
        assertThat(pending.getValue().getType()).isEqualTo(NotificationType.EMAIL);
        assertThat(pending.getValue().getEventId()).isEqualTo(EventType.MANAGE_HEARINGS.getCcdType());
    }

    @Test
    void givenNoPendingNotifications_whenUpdateSentAuditsList_thenEmptyMapIsReturned() {
        SendCorrespondenceEvent event = buildEvent();

        Map<String, Object> result = notificationAuditService.updateSentAuditsList(event);

        assertThat(result).isEmpty();
        verifyNoInteractions(objectMapper);
    }

    @Test
    void givenPendingNotificationMatchesSentAudit_whenUpdateSentAuditsList_thenAuditIsMarkedAsSent() {
        NotificationAudit pendingAudit = audit(NotificationParty.APPLICANT, NotificationType.EMAIL);
        NotificationAudit sentAudit = audit(NotificationParty.APPLICANT, NotificationType.EMAIL);

        SendCorrespondenceEvent event = buildEventWithPendingAndSentAudits(
            List.of(pendingItem(pendingAudit)),
            new ArrayList<>(List.of(sentAudit))
        );

        List<Map<String, Object>> convertedAudits = List.of(Map.of("converted", true));
        when(objectMapper.convertValue(any(Object.class), eq(List.class))).thenReturn(convertedAudits);

        Map<String, Object> result = notificationAuditService.updateSentAuditsList(event);

        assertThat(sentAudit.getWasSent()).isEqualTo(YesOrNo.YES);
        assertThat(result)
            .containsEntry(NOTIFICATIONS_AUDITS, convertedAudits)
            .containsEntry(NOTIFICATIONS_TO_BE_SENT, List.of());
    }

    @Test
    void givenPendingNotificationDoesNotMatchSentAudit_whenUpdateSentAuditsList_thenPendingAuditIsMarkedAsNotSent() {
        NotificationAudit pendingAudit = audit(NotificationParty.RESPONDENT, NotificationType.POSTAL);
        NotificationAudit sentAudit = audit(NotificationParty.RESPONDENT, NotificationType.EMAIL);

        List<NotificationAudit> sentAudits = new ArrayList<>(List.of(sentAudit));

        SendCorrespondenceEvent event = buildEventWithPendingAndSentAudits(
            List.of(pendingItem(pendingAudit)),
            sentAudits
        );

        when(objectMapper.convertValue(any(Object.class), eq(List.class)))
            .thenReturn(List.of(Map.of("converted", true)));

        notificationAuditService.updateSentAuditsList(event);

        assertThat(pendingAudit.getWasSent()).isEqualTo(YesOrNo.NO);
        assertThat(sentAudits).contains(pendingAudit);
    }

    private SendCorrespondenceEvent buildEvent() {
        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails(NotificationAuditWrapper.builder().build()))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .build();
    }

    private SendCorrespondenceEvent buildEventWithPendingAndSentAudits(
        List<NotificationToBeSentCollectionItem> pending,
        List<NotificationAudit> sentAudits
    ) {
        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails(NotificationAuditWrapper.builder()
                .notificationsToBeSent(pending)
                .build()))
            .notificationAudits(sentAudits)
            .build();
    }

    private FinremCaseDetails caseDetails(NotificationAuditWrapper wrapper) {
        return FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .notificationAuditWrapper(wrapper)
                .build())
            .build();
    }

    private NotificationAudit audit(NotificationParty party, NotificationType type) {
        return NotificationAudit.builder()
            .eventId(EventType.MANAGE_HEARINGS.getCcdType())
            .party(party.name())
            .type(type)
            .build();
    }

    private NotificationToBeSentCollectionItem pendingItem(NotificationAudit audit) {
        return NotificationToBeSentCollectionItem.builder()
            .value(audit)
            .build();
    }
}