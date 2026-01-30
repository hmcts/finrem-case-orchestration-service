package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class FormerRespondentBarristerListener extends EmailNotificationOnlyListener {

    public FormerRespondentBarristerListener(BulkPrintService bulkPrintService,
                                             EmailService emailService,
                                             NotificationService notificationService,
                                             InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "former respondent barrister";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.FORMER_RESPONDENT_BARRISTER_ONLY);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        return event.getCaseDetailsBefore() != null;
    }
}
