package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestObjectMapperFactory;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_AUDITS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_TO_BE_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR;

@ExtendWith(MockitoExtension.class)
class NotificationAuditServiceTest {

    private static final String NOTIFICATION_EVENT_ID = "notificationEventId";
    private static final String CURRENT_NOTIFICATION_EVENT_ID = "event-123";
    private static final String PREVIOUS_NOTIFICATION_EVENT_ID = "event-456";

    @Spy
    private ObjectMapper objectMapper = TestObjectMapperFactory.createObjectMapper();

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private NotificationAuditService notificationAuditService;

    @Test
    void givenNotificationsAreCreated_whenCreateAuditsForCorrespondence_thenPendingNotificationsShareEventTracker() {
        NotificationAudit previousPendingAudit = audit(
            NotificationParty.RESPONDENT,
            NotificationType.POSTAL,
            PREVIOUS_NOTIFICATION_EVENT_ID
        );

        NotificationAuditWrapper wrapper = NotificationAuditWrapper.builder()
            .notificationsToBeSent(List.of(pendingItem(previousPendingAudit)))
            .build();
        SendCorrespondenceEvent event = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails(wrapper))
            .emailTemplate(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR)
            .build();
        doAnswer(invocation -> {
            SendCorrespondenceEvent publishedEvent = invocation.getArgument(0);
            publishedEvent.recordEmailNotificationToSendAudit(NotificationParty.APPLICANT);
            publishedEvent.recordEmailNotificationToSendAudit(NotificationParty.RESPONDENT);
            return null;
        }).when(applicationEventPublisher).publishEvent(event);
        notificationAuditService.createAuditsForCorrespondence(
            event,
            EventType.MANAGE_HEARINGS
        );
        String notificationEventId = wrapper.getNotificationEventId();
        List<NotificationToBeSentCollectionItem> pending = wrapper.getNotificationsToBeSent();

        verify(applicationEventPublisher).publishEvent(event);
        assertThat(event.isSimulatingCorrespondence()).isTrue();
        assertThat(notificationEventId).isNotBlank();
        assertThat(event.getNotificationTrackerId()).isEqualTo(notificationEventId);
        assertThat(pending).hasSize(3);
        assertThat(pending.getFirst().getValue().getNotificationTrackerId())
            .isEqualTo(PREVIOUS_NOTIFICATION_EVENT_ID);
        List<NotificationToBeSentCollectionItem> newPending = pending.subList(
            1,
            pending.size()
        );

        assertThat(newPending)
            .extracting(item -> item.getValue().getNotificationTrackerId())
            .containsOnly(notificationEventId);
        assertThat(newPending)
            .extracting(item -> item.getValue().getWasSent())
            .containsOnly(YesOrNo.NO);
        assertThat(newPending)
            .extracting(item -> item.getValue().getEventId())
            .containsOnly(EventType.MANAGE_HEARINGS.getCcdType());
        assertThat(newPending)
            .extracting(item -> item.getValue().getEmailTemplate())
            .containsOnly(FR_CONTESTED_HEARING_NOTIFICATION_SOLICITOR.name());
    }

    @Test
    void givenPendingNotificationMatchesSentAudit_whenUpdateSentAuditsList_thenAuditIsMarkedAsSent() {
        NotificationAudit pendingAudit = audit(
            NotificationParty.APPLICANT,
            NotificationType.EMAIL,
            CURRENT_NOTIFICATION_EVENT_ID
        );
        NotificationAudit sentAudit = audit(
            NotificationParty.APPLICANT,
            NotificationType.EMAIL,
            CURRENT_NOTIFICATION_EVENT_ID
        );
        SendCorrespondenceEvent event = buildEventWithPendingAndSentAudits(
            List.of(pendingItem(pendingAudit)),
            new ArrayList<>(List.of(sentAudit))
        );
        Map<String, Object> result = notificationAuditService.updateSentAuditsList(event);
        assertThat(result)
            .containsEntry(NOTIFICATIONS_TO_BE_SENT, List.of())
            .containsEntry(NOTIFICATION_EVENT_ID, null);
        Map<String, Object> audit = firstNotificationAuditValue(result);
        assertThat(audit)
            .containsEntry("party", NotificationParty.APPLICANT.name())
            .containsEntry("type", "email")
            .containsEntry("wasSent", "Yes")
            .containsEntry(
                "eventId", EventType.MANAGE_HEARINGS.getCcdType())
            .containsEntry("notificationTrackerId", CURRENT_NOTIFICATION_EVENT_ID
            );
    }

    @Test
    void givenPendingNotificationDoesNotMatchSentAudit_whenUpdateSentAuditsList_thenPendingAuditIsAddedAsNotSent() {
        NotificationAudit pendingAudit = audit(
            NotificationParty.RESPONDENT,
            NotificationType.POSTAL,
            CURRENT_NOTIFICATION_EVENT_ID);
        NotificationAudit sentAudit = audit(
            NotificationParty.RESPONDENT,
            NotificationType.EMAIL,
            CURRENT_NOTIFICATION_EVENT_ID);
        SendCorrespondenceEvent event = buildEventWithPendingAndSentAudits(
            List.of(pendingItem(pendingAudit)),
            new ArrayList<>(List.of(sentAudit)));
        Map<String, Object> result = notificationAuditService.updateSentAuditsList(event);
        assertThat(result).containsEntry(NOTIFICATIONS_TO_BE_SENT, List.of());
        assertThat(notificationAuditValues(result)).anySatisfy(audit ->
            assertThat(audit)
                .containsEntry("party", NotificationParty.RESPONDENT.name())
                .containsEntry("type", "postal")
                .containsEntry("wasSent", "No")
                .containsEntry(
                    "eventId", EventType.MANAGE_HEARINGS.getCcdType())
                .containsEntry(
                    "notificationTrackerId",
                    CURRENT_NOTIFICATION_EVENT_ID
                )
        );
    }

    @Test
    void givenPreviousAndCurrentPendingNotifications_whenUpdateSentAuditsList_thenOnlyPreviousNotificationIsRetained() {
        NotificationAudit previousPendingAudit = audit(
            NotificationParty.RESPONDENT,
            NotificationType.POSTAL,
            PREVIOUS_NOTIFICATION_EVENT_ID);
        NotificationAudit currentPendingAudit = audit(
            NotificationParty.APPLICANT,
            NotificationType.EMAIL,
            CURRENT_NOTIFICATION_EVENT_ID);
        NotificationAudit currentSentAudit = audit(
            NotificationParty.APPLICANT,
            NotificationType.EMAIL,
            CURRENT_NOTIFICATION_EVENT_ID);
        SendCorrespondenceEvent event = buildEventWithPendingAndSentAudits(
            List.of(
                pendingItem(previousPendingAudit),
                pendingItem(currentPendingAudit)
            ),
            new ArrayList<>(List.of(currentSentAudit))
        );
        Map<String, Object> result = notificationAuditService.updateSentAuditsList(event);
        List<Map<String, Object>> remainingPending = notificationToBeSentValues(result);
        assertThat(remainingPending).hasSize(1);
        assertThat(remainingPending.getFirst())
            .containsEntry(
                "party",
                NotificationParty.RESPONDENT.name()
            ).containsEntry("type", "postal")
            .containsEntry("notificationTrackerId", PREVIOUS_NOTIFICATION_EVENT_ID);
        assertThat(result).containsEntry(NOTIFICATION_EVENT_ID, null);
    }

    private SendCorrespondenceEvent buildEventWithPendingAndSentAudits(
        List<NotificationToBeSentCollectionItem> pending,
        List<NotificationAudit> sentAudits
    ) {
        return SendCorrespondenceEvent.builder()
            .caseDetails(
                caseDetails(
                    NotificationAuditWrapper.builder()
                        .notificationEventId(NotificationAuditServiceTest.CURRENT_NOTIFICATION_EVENT_ID)
                        .notificationsToBeSent(pending)
                        .build()
                )
            )
            .notificationAudits(sentAudits)
            .build();
    }

    private FinremCaseDetails caseDetails(NotificationAuditWrapper wrapper) {
        return FinremCaseDetails.builder()
            .data(
                FinremCaseData.builder()
                    .notificationAuditWrapper(wrapper)
                    .build()
            )
            .build();
    }

    private NotificationAudit audit(
        NotificationParty party,
        NotificationType type,
        String notificationTrackerId
    ) {
        return NotificationAudit.builder()
            .wasSent(YesOrNo.NO)
            .eventId(EventType.MANAGE_HEARINGS.getCcdType())
            .party(party.name())
            .type(type)
            .notificationTrackerId(notificationTrackerId)
            .build();
    }

    private NotificationToBeSentCollectionItem pendingItem(
        NotificationAudit audit
    ) {
        return NotificationToBeSentCollectionItem.builder()
            .value(audit)
            .build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> notificationAuditItems(
        Map<String, Object> result
    ) {
        return (List<Map<String, Object>>) result.get(NOTIFICATIONS_AUDITS);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> notificationAuditValue(
        Map<String, Object> auditItem
    ) {
        return (Map<String, Object>) auditItem.get("value");
    }

    private Map<String, Object> firstNotificationAuditValue(
        Map<String, Object> result
    ) {
        return notificationAuditValue(
            notificationAuditItems(result).getFirst()
        );
    }

    private List<Map<String, Object>> notificationAuditValues(
        Map<String, Object> result
    ) {
        return notificationAuditItems(result).stream()
            .map(this::notificationAuditValue)
            .toList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> notificationToBeSentItems(
        Map<String, Object> result
    ) {
        return (List<Map<String, Object>>) result.get(NOTIFICATIONS_TO_BE_SENT);
    }

    private List<Map<String, Object>> notificationToBeSentValues(
        Map<String, Object> result
    ) {
        return notificationToBeSentItems(result).stream()
            .map(this::notificationAuditValue)
            .toList();
    }
}
