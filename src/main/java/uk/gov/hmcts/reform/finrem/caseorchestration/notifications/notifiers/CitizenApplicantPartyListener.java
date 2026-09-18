package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
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
        FinremCaseData caseData = event.getCaseDetails().getData();
        return caseData != null
            && caseData.getContactDetailsWrapper().getApplicantEmail() != null
            && !caseData.getContactDetailsWrapper().getApplicantEmail().isBlank();
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        FinremCaseData caseData = event.getCaseDetails().getData();
        return new PartySpecificDetails(
            caseData.getContactDetailsWrapper().getApplicantEmail(),
            caseData.getFullApplicantName(),
            null
        );
    }
}
