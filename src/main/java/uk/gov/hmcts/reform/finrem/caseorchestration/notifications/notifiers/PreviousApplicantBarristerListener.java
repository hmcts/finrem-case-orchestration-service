package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static java.util.Optional.ofNullable;

@Component
public class PreviousApplicantBarristerListener extends EmailNotificationOnlyListener {

    public PreviousApplicantBarristerListener(BulkPrintService bulkPrintService,
                                              EmailService emailService,
                                              NotificationService notificationService,
                                              InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected String getNotificationParty() {
        return "previous applicant barrister";
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.PREVIOUS_APPLICANT_BARRISTER_ONLY);
    }

    @Override
    protected boolean shouldSendEmailNotification(SendCorrespondenceEvent event) {
        if (event.getCaseDetailsBefore() != null) {
            return notificationService.isApplicantSolicitorEmailPopulatedAndPresented(event.getCaseDetailsBefore());
        } else {
            return false;
        }
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        String email = event.getEmailNotificationRequest().getNotificationEmail();
        String name = event.getEmailNotificationRequest().getName();
        String ref = ofNullable(event.getEmailNotificationRequest().getSolicitorReferenceNumber()).orElse("");
        return new PartySpecificDetails(email, name, ref);
    }
}
