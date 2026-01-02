package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@RequiredArgsConstructor
public abstract class AbstractPartyListener {

    protected abstract boolean isRelevantParty(SendCorrespondenceEvent event);
    protected abstract boolean isDigitalParty(SendCorrespondenceEvent event);
    protected abstract void sendLetterNotification(SendCorrespondenceEvent event);
    protected abstract PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event);

    protected final BulkPrintService bulkPrintService;
    protected final EmailService emailService;
    protected final NotificationService notificationService;

    protected record PartySpecificDetails(
        String recipientEmailAddress,
        String recipientName
    ) {}

    @Async
    @EventListener
    public void handleNotification(SendCorrespondenceEvent event) {
        if (isRelevantParty(event) ) {
            sendNotification(event);
        }
    }

    private void sendNotification(SendCorrespondenceEvent event) {
        if (isDigitalParty(event)) {
            NotificationRequest emailRequest = event.emailNotificationRequest;
            PartySpecificDetails partySpecificDetails = setPartySpecificDetails(event);
            emailRequest.setName(partySpecificDetails.recipientName);
            emailRequest.setNotificationEmail(partySpecificDetails.recipientEmailAddress);
            sendEmailNotification(event);
        } else {
           sendLetterNotification(event);
        }
    }

    private void sendEmailNotification(SendCorrespondenceEvent event) {
        emailService.sendConfirmationEmail(event.getEmailNotificationRequest(),
            event.emailTemplateId);
    }
}
