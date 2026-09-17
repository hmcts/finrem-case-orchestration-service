package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class CitizenRespondentPartyListener extends EmailNotificationOnlyListener {

    public CitizenRespondentPartyListener(BulkPrintService bulkPrintService,
                                          EmailService emailService,
                                          NotificationService notificationService,
                                          InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected NotificationParty getNotificationPartyEnum() {
        return NotificationParty.CITIZEN_RESPONDENT;
    }

    @Override
    protected String getNotificationParty() {
        return "citizen respondent";
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        return event.getEmailNotificationRequest() != null
            && event.getEmailNotificationRequest().getNotificationEmail() != null
            && !event.getEmailNotificationRequest().getNotificationEmail().isBlank();
    }
}
