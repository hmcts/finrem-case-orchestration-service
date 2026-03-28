package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

/**
 * լս Listener responsible for handling correspondence notifications
 * specifically for the applicant's solicitor.
 *
 * <p>This listener extends {@link EmailNotificationOnlyListener} and determines:
 * <ul>
 *     <li>Whether the applicant solicitor is the intended notification party</li>
 *     <li>Whether an email notification should be sent based on available case details</li>
 * </ul>
 *
 * <p>Email notifications are only sent when the applicant solicitor's email
 * is present and marked as valid within the case data.
 */
@Component
public class ApplicantSolicitorListener extends EmailNotificationOnlyListener {

    public ApplicantSolicitorListener(BulkPrintService bulkPrintService,
                                      EmailService emailService,
                                      NotificationService notificationService,
                                      InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "applicant solicitor";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.APPLICANT_SOLICITOR_ONLY);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        if (event.getCaseDetails() != null) {
            return notificationService.isApplicantSolicitorEmailPopulatedAndPresented(event.getCaseDetails());
        } else {
            return false;
        }
    }
}
