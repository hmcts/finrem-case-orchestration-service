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
import java.util.Collections;
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

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates notification audit rows for the correspondence event.
     * The correspondence process is simulated first so listeners can determine
     * which parties would receive correspondence by email or post without sending it.
     * One audit row is created per notification party, and each pending audit
     * is stored for later sent-status updates.
     *
     * @param event     the correspondence event containing parties, case details, template and documents
     * @param eventType the event triggering the notification, used for the eventId field
     */
    public void createAuditsForCorrespondence(SendCorrespondenceEvent event,
                                              EventType eventType) {
        event.setEventId(eventType.getCcdType());
        event.setSimulatingCorrespondence(true);
        applicationEventPublisher.publishEvent(event);

        FinremCaseData caseData = event.getCaseData();
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        List<NotificationToBeSentCollectionItem> pending = event.getNotificationAudits().stream()
            .map(audit -> NotificationToBeSentCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(audit)
                .build())
            .toList();

        wrapper.setNotificationsToBeSent(pending);
    }

    /**
     * Updates the notification audit history after correspondence has been sent.
     *
     * The pending audits created during About To Submit are compared with the audits
     * recorded during Submitted. Successfully sent notifications are already present
     * with wasSent set to Yes. Any pending audit without a matching sent audit is added
     * with its existing wasSent value of No. Audits are matched using the party,
     * notification type and event ID.
     *
     * Existing audit history is preserved, the completed audits for the current event
     * are appended, and the pending notifications collection is cleared.
     *
     * Returns an empty map when there are no pending notifications to process.
     *
     * @param sentEvent the correspondence event containing the successfully sent notification audits
     * @return the CCD fields containing the updated audit history and cleared pending notifications
     */
    public Map<String, Object> updateSentAuditsList(SendCorrespondenceEvent sentEvent) {
        FinremCaseData caseData = sentEvent.getCaseData();
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        List<NotificationAudit> audits = sentEvent.getNotificationAudits();
        List<NotificationToBeSentCollectionItem> pending = wrapper.getNotificationsToBeSent();

        if (pending == null || pending.isEmpty()) {
            return Collections.emptyMap();
        }

        combinePendingAndSentAudits(pending, audits);

        List<NotificationAuditCollectionItem> auditItems = new ArrayList<>(

            Optional.ofNullable(wrapper.getNotificationsAudits())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getValue() != null)
                .toList()
        );

        audits.stream()
            .filter(Objects::nonNull)
            .map(audit -> NotificationAuditCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(audit)
                .build())
            .forEach(auditItems::add);

        return Map.of(
            NOTIFICATIONS_AUDITS, objectMapper.convertValue(auditItems, List.class),
            NOTIFICATIONS_TO_BE_SENT, List.of()
        );
    }

    private void combinePendingAndSentAudits(
        List<NotificationToBeSentCollectionItem> pending,
        List<NotificationAudit> audits
    ) {
        pending.stream()
            .map(NotificationToBeSentCollectionItem::getValue)
            .filter(Objects::nonNull)
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
            && Objects.equals(expected.getEventId(), actual.getEventId());
    }
}
