package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class CitizenApplicantPartyListener extends EmailNotificationOnlyListener {

    public CitizenApplicantPartyListener(BulkPrintService bulkPrintService,
                                         EmailService emailService,
                                         NotificationService notificationService,
                                         InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected NotificationParty getNotificationPartyEnum() {
        return NotificationParty.CITIZEN_APPLICANT;
    }

    @Override
    protected String getNotificationParty() {
        return "citizen applicant";
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        return event.getEmailNotificationRequest() != null
            && event.getEmailNotificationRequest().getNotificationEmail() != null
            && !event.getEmailNotificationRequest().getNotificationEmail().isBlank();
    }
}
