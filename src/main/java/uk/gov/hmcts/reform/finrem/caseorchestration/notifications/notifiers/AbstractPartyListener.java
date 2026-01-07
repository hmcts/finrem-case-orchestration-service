package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles party-specific notification logic; subclass for different party types.
 */
@RequiredArgsConstructor
public abstract class AbstractPartyListener {

    protected final BulkPrintService bulkPrintService;
    protected final EmailService emailService;
    protected final NotificationService notificationService;
    protected final InternationalPostalService internationalPostalService;

    /**
     * Does this listener handle notifications for this party/event?
     */
    protected abstract boolean isRelevantParty(SendCorrespondenceEvent event);

    /**
     * Should the notification be sent digitally (by email)?
     */
    protected abstract boolean isDigitalParty(SendCorrespondenceEvent event);

    /**
     * Returns email/name details for the party.
     */
    protected abstract PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event);

    /**
     * Gets the coversheet document for the party.
     */
    protected abstract CaseDocument getPartyCoversheet(SendCorrespondenceEvent event);

    /**
     * Performs the actual print-and-send operation.
     */
    protected abstract void sendLetter(SendCorrespondenceEvent event,
                                       List<BulkPrintDocument> bulkPrintDocs,
                                       boolean isOutsideUK);

    /**
     * Returns true if the party resides outside the UK.
     */
    protected abstract boolean isPartyOutsideUK(SendCorrespondenceEvent event);

    /**
     * Struct for holding party-specific contact details.
     */
    protected record PartySpecificDetails(
        String recipientEmailAddress,
        String recipientName
    ) {}

    @Async
    @EventListener
    public void handleNotification(SendCorrespondenceEvent event) {
        if (isRelevantParty(event)) {
            sendNotification(event);
        }
    }

    private void sendNotification(SendCorrespondenceEvent event) {
        if (isDigitalParty(event)) {
            enrichAndSendEmailNotification(event);
        } else {
            handlePaperNotification(event);
        }
    }

    /**
     * Prepares and sends an email notification to the relevant party.
     */
    private void enrichAndSendEmailNotification(SendCorrespondenceEvent event) {
        PartySpecificDetails details = setPartySpecificDetails(event);
        NotificationRequest emailRequest = event.emailNotificationRequest;
        emailRequest.setName(details.recipientName());
        emailRequest.setNotificationEmail(details.recipientEmailAddress());
        emailService.sendConfirmationEmail(emailRequest, event.emailTemplateId);
    }

    /**
     * Collect docs, coversheet, and send letter for the relevant party.
     */
    private void handlePaperNotification(SendCorrespondenceEvent event) {
        // Defensive copy to avoid mutating original event collection
        List<CaseDocument> docsToPrint = new ArrayList<>(event.documentsToPost);
        docsToPrint.add(getPartyCoversheet(event));
        List<BulkPrintDocument> bpDocs = bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(docsToPrint);
        boolean isOutsideUK = isPartyOutsideUK(event);
        sendLetter(event, bpDocs, isOutsideUK);
    }
}
