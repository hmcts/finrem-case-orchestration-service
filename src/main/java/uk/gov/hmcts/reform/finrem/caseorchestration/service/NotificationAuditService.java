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
     * The event is published in dry-run mode first so listeners can determine
     * whether each party would receive correspondence by email or post.
     * One audit row is created per notification party, and each audit row ID
     * is added to the pending notifications list for later sent-status updates.
     *
     * @param event     the correspondence event containing parties, case details, template and documents
     * @param eventType the event triggering the notification, used for the eventId field
     */
    public void createAuditsForCorrespondence(SendCorrespondenceEvent event,
                                              EventType eventType) {
        event.setEventId(eventType.getCcdType());
        event.setDryRun(true);
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
     * Updates the notification audit history for a sent correspondence event.
     *
     * <p>This method compares the pending notifications stored on the case with the
     * notification audits recorded on the sent event. If a pending notification has
     * a matching sent audit, the sent audit is marked as successfully sent. If no
     * matching sent audit is found, the pending audit is marked as not sent and added
     * to the current event audit list.</p>
     *
     * <p>Existing notification audit rows already stored on the case are preserved.
     * The returned audit collection is made up of the existing audit history plus the
     * final audit rows for the current sent event.</p>
     *
     * <p>The returned map also clears the pending notifications collection. If there
     * are no pending notifications, an empty map is returned and no case update is
     * required.</p>
     *
     * @param sentEvent the correspondence event containing the notification audits recorded during sending
     * @return a map of CCD fields to update, or an empty map if there are no pending notifications
     */
    public Map<String, Object> updateSentAuditsList(SendCorrespondenceEvent sentEvent) {
        FinremCaseData caseData = sentEvent.getCaseData();
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        List<NotificationAudit> audits = sentEvent.getNotificationAudits();
        List<NotificationToBeSentCollectionItem> pending = wrapper.getNotificationsToBeSent();

        if (pending == null || pending.isEmpty()) {
            return Collections.emptyMap();
        }


        pending.stream()
            .map(NotificationToBeSentCollectionItem::getValue)
            .filter(Objects::nonNull)
            .forEach(pendingAudit ->
                audits.stream()
                    .filter(audit -> isSameNotification(pendingAudit, audit))
                    .findFirst()
                    .ifPresentOrElse(
                        audit -> audit.setWasSent(YesOrNo.YES),
                        () -> {
                            pendingAudit.setWasSent(YesOrNo.NO);
                            audits.add(pendingAudit);
                        }
                    )
            );

        List<NotificationAudit> allAudits = new ArrayList<>();

        Optional.ofNullable(wrapper.getNotificationsAudits())
            .orElseGet(List::of)
            .stream()
            .map(NotificationAuditCollectionItem::getValue)
            .filter(Objects::nonNull)
            .forEach(allAudits::add);

        allAudits.addAll(audits);

        List<NotificationAuditCollectionItem> auditItems = allAudits.stream()
            .map(audit -> NotificationAuditCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(audit)
                .build())
            .toList();

        return Map.of(
            NOTIFICATIONS_AUDITS, objectMapper.convertValue(auditItems, List.class),
            NOTIFICATIONS_TO_BE_SENT, List.of()
        );
    }

    private boolean isSameNotification(NotificationAudit expected,
                                       NotificationAudit actual) {
        return Objects.equals(expected.getParty(), actual.getParty())
            && Objects.equals(expected.getType(), actual.getType())
            && Objects.equals(expected.getEventId(), actual.getEventId());
    }
}
