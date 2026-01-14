package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles party-specific notification logic; subclass for different party types.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPartyListener {

    protected final BulkPrintService bulkPrintService;
    protected final EmailService emailService;
    protected final NotificationService notificationService;
    protected final InternationalPostalService internationalPostalService;

    protected String notificationParty;

    /**
     * Should this listener handle notifications for this party/event.
     */
    protected abstract boolean isRelevantParty(SendCorrespondenceEvent event);

    /**
     * Should the notification be sent digitally (by email).
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
     * Returns true if the party resides outside the UK.
     */
    protected abstract boolean isPartyOutsideUK(SendCorrespondenceEvent event);

    /**
     * Struct for holding party-specific contact details.
     */
    protected record PartySpecificDetails(
        @NotNull String recipientSolEmailAddress,
        @NotNull String recipientSolName,
        @NotNull String recipientSolReference
    ) {
        public PartySpecificDetails {
            if (recipientSolEmailAddress == null || recipientSolName == null || recipientSolReference == null) {
                throw new IllegalArgumentException("PartySpecificDetails fields must not be null");
            }
        }
    }

    @Async
    @EventListener
    public void handleNotification(SendCorrespondenceEvent event) {
        if (isRelevantParty(event)) {
            log.info("Notification event received for party {} on case case {}", notificationParty, event.getCaseId());
            sendNotification(event);
        }
    }

    private void sendNotification(SendCorrespondenceEvent event) {
        if (isDigitalParty(event)) {
            enrichAndSendEmailNotification(event);
        } else {
            sendPaperNotification(event);
        }
    }

    /**
     * Enriches the email notification data with party-specific details and sends a confirmation email.
     *
     * @param event the event containing the notification details, including the email template and notification request.
     *              If the email template or notification request is missing, an {@link IllegalStateException} is thrown.
     */
    private void enrichAndSendEmailNotification(SendCorrespondenceEvent event) {

        log.info("Preparing email notification for party {} on case case {}", notificationParty, event.getCaseId());
        PartySpecificDetails details = setPartySpecificDetails(event);

        NotificationRequest emailRequest = Optional.ofNullable(event.getEmailNotificationRequest())
            .orElseThrow(() ->
                new IllegalArgumentException("Notification Request is required for digital notifications, case ID: " + event.getCaseId()));

        emailRequest.setName(details.recipientSolName);
        emailRequest.setNotificationEmail(details.recipientSolEmailAddress);
        emailRequest.setSolicitorReferenceNumber(details.recipientSolReference);

        EmailTemplateNames emailTemplate = Optional.ofNullable(event.getEmailTemplate()).orElseThrow(() ->
            new IllegalArgumentException("Email template is required for digital notifications, case ID: " + event.getCaseId()));

        // Email service handles email specific exceptions - consider building in retries to email service.
        emailService.sendConfirmationEmail(emailRequest, emailTemplate);

        log.info("Completed email notification for party {} on case case {}", notificationParty, event.getCaseId());
    }

    /**
     * Sends a paper notification for the specified event by preparing the necessary documents
     * for bulk printing and invoking the bulk print service. This includes adding a coversheet
     * specific to the party and determining if the party resides outside the UK to handle
     * international notifications accordingly.
     *
     * @param event the event containing details necessary for sending the paper notification,
     *              such as the case details, documents to post, and authorization token.
     *              If no documents are provided, an {@link IllegalArgumentException} will be thrown.
     */
    private void sendPaperNotification(SendCorrespondenceEvent event) {

        log.info("Preparing paper notification for party {} on case case {}", notificationParty, event.getCaseId());

        // Defensive copy to avoid mutating an original event collection
        List<CaseDocument> docsToPrint = Optional.ofNullable(event.documentsToPost)
            .filter(docs -> !docs.isEmpty())
            .map(ArrayList::new)
            .orElseThrow(() ->
                new IllegalArgumentException("No documents to post provided for paper notification, case ID: " + event.getCaseId()));

        docsToPrint.add(getPartyCoversheet(event));
        List<BulkPrintDocument> bpDocs = bulkPrintService.convertCaseDocumentsToBulkPrintDocuments(docsToPrint);
        boolean isOutsideUK = isPartyOutsideUK(event);

        // Bulk print service requires implementation of exception handling -
        // consider building in retries and Server Error Handling as part of DFR-3308.
        bulkPrintService.bulkPrintFinancialRemedyLetterPack(
            event.caseDetails, notificationParty, bpDocs, isOutsideUK, event.authToken
        );

        log.info("Completed paper notification for party {} on case case {}", notificationParty, event.getCaseId());
    }
}
