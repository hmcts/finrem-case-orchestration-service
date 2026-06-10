package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
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
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService.FINANCIAL_REMEDY_PACK_LETTER_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationAuditService {

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

        FinremCaseDetails caseDetails = event.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        NotificationAuditWrapper wrapper = caseData.getNotificationAuditWrapper();

        List<NotificationAuditCollectionItem> audits = Optional.ofNullable(wrapper.getNotificationsAudits())
            .orElseGet(ArrayList::new);

        List<NotificationToBeSentCollectionItem> pending = Optional.ofNullable(wrapper.getNotificationsToBeSent())
            .orElseGet(ArrayList::new);

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
        return Optional.ofNullable(documents)
            .orElseGet(List::of)
            .stream()
            .map(CaseDocument::getDocumentFilename)
            .toList();
    }
}

