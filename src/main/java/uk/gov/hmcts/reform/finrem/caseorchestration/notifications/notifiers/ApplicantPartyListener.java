package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
public class ApplicantPartyListener extends AbstractPartyListener {

    public ApplicantPartyListener(BulkPrintService bulkPrintService, EmailService emailService, NotificationService notificationService) {
        super(bulkPrintService, emailService, notificationService);
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.APPLICANT);
    }

    @Override
    protected boolean isDigitalParty(SendCorrespondenceEvent event) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(event.getCaseDetails());
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        String email = event.getCaseDetails().getAppSolicitorEmail();
        String name = event.getCaseDetails().getAppSolicitorName();
        return new PartySpecificDetails(email, name);
    }

    @Override
    protected void sendLetterNotification(SendCorrespondenceEvent event) {
        //TODO: implement letter sending for applicant
    }
}
