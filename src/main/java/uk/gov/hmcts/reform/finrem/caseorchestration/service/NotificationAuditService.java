package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
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
            event.getNotificationAudits().stream()
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

    public Map<String, Object> updateSentAuditsList(SendCorrespondenceEvent sentEvent) {
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
            Optional.ofNullable(wrapper.getNotificationsToBeSent())
                .orElseGet(List::of);

        /*
         * Only process pending notifications belonging to this event execution.
         */
        List<NotificationToBeSentCollectionItem> currentEventPending =
            pending.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getValue() != null)
                .filter(item -> Objects.equals(
                    currentNotificationEventId,
                    item.getValue().getNotificationTrackerId()
                ))
                .toList();

        List<NotificationAudit> audits = new ArrayList<>(
            Optional.ofNullable(sentEvent.getNotificationAudits())
                .orElseGet(List::of)
        );

        combinePendingAndSentAudits(currentEventPending, audits);

        /*
         * Preserve existing permanent audit history.
         */
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

        List<NotificationToBeSentCollectionItem> remainingPending =
            pending.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getValue() == null
                    || !Objects.equals(
                    currentNotificationEventId,
                    item.getValue().getNotificationTrackerId()
                ))
                .toList();

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

    private void combinePendingAndSentAudits(
        List<NotificationToBeSentCollectionItem> pending,
        List<NotificationAudit> audits
    ) {
        pending.stream()
            .map(NotificationToBeSentCollectionItem::getValue)
            .forEach(pendingAudit ->
                audits.stream()
                    .filter(sentAudit -> isSameNotification(pendingAudit, sentAudit))
                    .findFirst()
                    .ifPresentOrElse(
                        sentAudit -> sentAudit.setWasSent(YesOrNo.YES),
                        () -> audits.add(pendingAudit)
                    )
            );
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
