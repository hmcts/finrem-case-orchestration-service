package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class PreviousApplicantSolicitorListener extends EmailNotificationOnlyListener {

    public PreviousApplicantSolicitorListener(BulkPrintService bulkPrintService,
                                              EmailService emailService,
                                              NotificationService notificationService,
                                              InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "previous applicant solicitor";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.PREVIOUS_APPLICANT_SOLICITOR_ONLY);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        if (event.getCaseDetailsBefore() != null) {
            return notificationService.isApplicantSolicitorEmailPopulatedAndPresented(event.getCaseDetailsBefore());
        } else {
            return false;
        }
    }
}
