package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAuditCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationToBeSentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.NotificationAuditWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_AUDITS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.NOTIFICATIONS_TO_BE_SENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

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
        event.setDryRun(true);

        applicationEventPublisher.publishEvent(event);

        FinremCaseData caseData = event.getCaseData();
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        List<NotificationAuditCollectionItem> audits = new ArrayList<>(emptyIfNull(wrapper.getNotificationsAudits()));

        List<NotificationToBeSentCollectionItem> pending = new ArrayList<>(emptyIfNull(wrapper.getNotificationsToBeSent()));

        List<String> postalDocFilenames = filenamesOf(event.getDocumentsToPost());

        for (NotificationParty notificationParty : event.getNotificationParties()) {
            NotificationType channel = event.getNotificationTypeForParty(notificationParty);

            NotificationAudit auditRow = buildAuditRow(
                eventType,
                notificationParty.name(),
                channel,
                event.getEmailTemplate(),
                postalDocFilenames
            );

            UUID rowId = UUID.randomUUID();

            audits.add(NotificationAuditCollectionItem.builder()
                .id(rowId)
                .value(auditRow)
                .build());

            pending.add(NotificationToBeSentCollectionItem.builder()
                .id(UUID.randomUUID())
                .value(rowId)
                .build());
        }

        wrapper.setNotificationsAudits(audits);
        wrapper.setNotificationsToBeSent(pending);
    }

    /**
     * Marks pending notification audit rows as sent based on the correspondence event that was actually published.
     *
     * <p>The pending notification list stores audit row IDs inside
     * {@link NotificationToBeSentCollectionItem#getValue()}. For each pending item, this method finds
     * the matching audit row. If the corresponding party was recorded on the sent correspondence event,
     * the audit row is updated to {@link YesOrNo#YES}. If the party was not recorded on the event, the audit
     * row remains unchanged, usually as {@link YesOrNo#NO}.</p>
     *
     * <p>The pending notification queue is cleared after processing so it is ready for the next event run.</p>
     *
     * @param latestData the latest case data containing notification audit rows and pending notification IDs
     * @param sentEvent the correspondence event that was published in the submitted callback
     * @return a map of CCD field updates, or an empty map when there are no pending notifications
     */
    public Map<String, Object> markPendingNotificationsAsSent(FinremCaseData latestData,
                                                              SendCorrespondenceEvent sentEvent) {
        NotificationAuditWrapper wrapper = latestData.getNotificationAuditWrapper();

        List<NotificationAuditCollectionItem> audits =
            new ArrayList<>(emptyIfNull(wrapper.getNotificationsAudits()));

        List<NotificationToBeSentCollectionItem> pending =
            emptyIfNull(wrapper.getNotificationsToBeSent());

        if (pending.isEmpty()) {
            return Map.of();
        }

        pending.forEach(pendingItem ->
            audits.stream()
                .filter(audit -> audit.getId() != null)
                .filter(audit -> audit.getId().equals(pendingItem.getValue()))
                .findFirst()
                .ifPresent(audit -> getRecordedNotification(sentEvent, audit)
                    .ifPresent(recordedNotification -> {
                        audit.getValue().setWasSent(YesOrNo.YES);

                        Optional.ofNullable(recordedNotification.letterId())
                            .ifPresent(audit.getValue()::setLetterId);
                    }))
        );

        return Map.of(
            NOTIFICATIONS_AUDITS, objectMapper.convertValue(audits, List.class),
            NOTIFICATIONS_TO_BE_SENT, List.of()
        );
    }

    private Optional<SendCorrespondenceEvent.RecordedNotification> getRecordedNotification(
        SendCorrespondenceEvent sentEvent,
        NotificationAuditCollectionItem audit
    ) {
        if (sentEvent == null || audit == null || audit.getValue() == null || audit.getValue().getParty() == null) {
            return Optional.empty();
        }

        try {
            NotificationParty notificationParty = NotificationParty.valueOf(audit.getValue().getParty());
            return Optional.of(sentEvent.getRecordedNotificationForParty(notificationParty));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return Optional.empty();
        }
    }

    private NotificationAudit buildAuditRow(EventType eventType,
                                            String partyRole,
                                            NotificationType channel,
                                            EmailTemplateNames emailTemplate,
                                            List<String> postalDocFilenames) {

        NotificationAudit.NotificationAuditBuilder builder = NotificationAudit.builder()
            .createdAt(LocalDate.now())
            .wasSent(YesOrNo.NO)
            .eventId(eventType.getCcdType())
            .party(partyRole)
            .type(channel);

        if (channel == NotificationType.EMAIL) {
            builder.emailTemplate(emailTemplate == null ? null : emailTemplate.name());
        } else {
            builder.letterTemplate(FINANCIAL_REMEDY_PACK_LETTER_TYPE);
            builder.attachedPostalDocs(String.join(", ", postalDocFilenames));
        }

        return builder.build();
    }

    private List<String> filenamesOf(List<CaseDocument> documents) {
        return emptyIfNull(documents).stream()
            .map(CaseDocument::getDocumentFilename)
            .toList();
    }
}
