package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAuditCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_AUDITS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_TO_BE_SENT;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAuditService {

    private static final String NOTIFICATION_EVENT_ID = "notificationEventId";

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates notification audit entries for correspondence that is about to be sent.
     * A unique notification event ID is generated and associated with both the case data
     * and the {@link SendCorrespondenceEvent}. The event is then published and the
     * notification audits are added to the pending notifications list.
     *
     * @param event the correspondence event containing the case data and notification audits
     * @param eventType the CCD event type associated with the correspondence
     * @throws IllegalStateException if the correspondence event does not contain case data
     */
    public void createAuditsForCorrespondence(SendCorrespondenceEvent event,
                                              EventType eventType) {
        FinremCaseData caseData = event.getCaseData();
        if (caseData == null) {
            throw new IllegalStateException("No caseData found when creating notification audits");
        }
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        String notificationEventId = UUID.randomUUID().toString();

        wrapper.setNotificationEventId(notificationEventId);

        event.setNotificationTrackerId(notificationEventId);
        event.setEventId(eventType.getCcdType());
        event.setSimulatingCorrespondence(true);
        applicationEventPublisher.publishEvent(event);

        List<NotificationToBeSentCollectionItem> pending =
            event.getAudits().stream()
                .map(audit -> NotificationToBeSentCollectionItem.builder()
                    .id(UUID.randomUUID())
                    .value(audit)
                    .build())
                .toList();

        List<NotificationToBeSentCollectionItem> allPending = new ArrayList<>(
            Optional.ofNullable(wrapper.getNotificationsToBeSent())
                .orElseGet(List::of)
        );

        allPending.addAll(pending);

        wrapper.setNotificationsToBeSent(allPending);
    }

    /**
     * Updates notification audit data after correspondence has been processed.
     * Matches notifications expected for the current notification event against
     * notifications actually produced during the submitted event using the party,
     * notification type and notification tracker ID.
     *
     * <p>
     * Once the current event has been processed, its pending notification records
     * are removed from the pending list. Any records already in the pending list
     * from an earlier event are preserved because they may be there as a result
     * of that event's submitted handler failing before the records could be
     * processed. This ensures that if the submitted handler failed for an earlier
     * event, its pending notification records are not accidentally deleted by a
     * later event.
     * </p>
     *
     * <p>
     * The notification event ID is cleared once processing is complete.
     * </p>
     *
     * @return a map containing the CCD case data fields that need to be updated,
     *         or an empty map when case data or the notification event ID is unavailable
     */
    public Map<String, Object> reconcileNotificationAudits(SendCorrespondenceEvent sentEvent) {
        FinremCaseData caseData = sentEvent.getCaseData();
        if (caseData == null) {
            log.warn("No caseData found when updating notification audits");
            return Map.of();
        }

        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();
        String currentNotificationEventId = wrapper.getNotificationEventId();

        if (currentNotificationEventId == null) {
            log.warn("No notificationEventId found when updating notification audits");
            return Map.of();
        }

        List<NotificationToBeSentCollectionItem> pending =
            getPendingNotifications(wrapper);

        List<NotificationToBeSentCollectionItem> currentEventPending =
            getCurrentEventPendingNotifications(
                pending,
                currentNotificationEventId
            );

        List<NotificationAudit> audits = getSentAudits(sentEvent);

        combinePendingAndSentAudits(currentEventPending, audits);

        List<NotificationAuditCollectionItem> auditItems =
            addAuditsToExistingHistory(wrapper, audits);

        List<NotificationToBeSentCollectionItem> remainingPending =
            removeCurrentEventPendingNotifications(
                pending,
                currentNotificationEventId
            );

        return buildUpdatedFields(auditItems, remainingPending);
    }

    private List<NotificationToBeSentCollectionItem> getPendingNotifications(
        NotificationAuditWrapper wrapper
    ) {
        return Optional.ofNullable(wrapper.getNotificationsToBeSent())
            .orElseGet(List::of);
    }

    private List<NotificationToBeSentCollectionItem> getCurrentEventPendingNotifications(
        List<NotificationToBeSentCollectionItem> pending,
        String notificationEventId
    ) {
        return pending.stream()
            .filter(Objects::nonNull)
            .filter(item -> item.getValue() != null)
            .filter(item -> Objects.equals(
                notificationEventId,
                item.getValue().getNotificationTrackerId()
            ))
            .toList();
    }

    private List<NotificationAudit> getSentAudits(
        SendCorrespondenceEvent sentEvent
    ) {
        return new ArrayList<>(
            Optional.ofNullable(sentEvent.getAudits())
                .orElseGet(List::of)
        );
    }

    private void combinePendingAndSentAudits(
        List<NotificationToBeSentCollectionItem> pending,
        List<NotificationAudit> audits
    ) {
        pending.stream()
            .map(NotificationToBeSentCollectionItem::getValue)
            .forEach(pendingAudit -> {
                boolean notificationWasSent = audits.stream()
                    .anyMatch(sentAudit -> isSameNotification(pendingAudit, sentAudit));

                if (!notificationWasSent) {
                    audits.add(pendingAudit);
                }
            });
    }

    private List<NotificationAuditCollectionItem> addAuditsToExistingHistory(
        NotificationAuditWrapper wrapper,
        List<NotificationAudit> audits
    ) {
        List<NotificationAuditCollectionItem> auditItems = new ArrayList<>(
            Optional.ofNullable(wrapper.getNotificationsAudits())
                .orElseGet(List::of)
        );

        audits.stream()
            .map(audit -> NotificationAuditCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(audit)
                .build())
            .forEach(auditItems::add);

        return auditItems;
    }

    private List<NotificationToBeSentCollectionItem> removeCurrentEventPendingNotifications(
        List<NotificationToBeSentCollectionItem> pending,
        String notificationEventId
    ) {
        return pending.stream()
            .filter(Objects::nonNull)
            .filter(item -> item.getValue() == null
                || !Objects.equals(
                notificationEventId,
                item.getValue().getNotificationTrackerId()
            ))
            .toList();
    }

    private Map<String, Object> buildUpdatedFields(
        List<NotificationAuditCollectionItem> auditItems,
        List<NotificationToBeSentCollectionItem> remainingPending
    ) {
        Map<String, Object> updatedFields = new HashMap<>();

        updatedFields.put(
            NOTIFICATIONS_AUDITS,
            objectMapper.convertValue(auditItems, List.class)
        );

        updatedFields.put(
            NOTIFICATIONS_TO_BE_SENT,
            objectMapper.convertValue(remainingPending, List.class)
        );

        updatedFields.put(NOTIFICATION_EVENT_ID, null);

        return updatedFields;
    }

    private boolean isSameNotification(NotificationAudit expected,
                                       NotificationAudit actual) {
        return Objects.equals(expected.getParty(), actual.getParty())
            && Objects.equals(expected.getType(), actual.getType())
            && Objects.equals(
            expected.getNotificationTrackerId(),
            actual.getNotificationTrackerId()
        );
    }
}
