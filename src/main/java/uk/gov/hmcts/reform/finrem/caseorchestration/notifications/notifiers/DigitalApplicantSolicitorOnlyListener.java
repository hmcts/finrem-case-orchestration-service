package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static com.google.common.base.Strings.nullToEmpty;

@Component
public class DigitalApplicantSolicitorOnlyListener extends DigitalOnlyListener {

    public DigitalApplicantSolicitorOnlyListener(BulkPrintService bulkPrintService,
                                                 EmailService emailService,
                                                 NotificationService notificationService,
                                                 InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.APPLICANT_SOLICITOR_ONLY);
    }

    @Override
    protected boolean isDigitalParty(SendCorrespondenceEvent event) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(event.getCaseDetails());
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        FinremCaseDetails caseDetails = event.getCaseDetails();
        String email = caseDetails.getAppSolicitorEmail();
        String name = caseDetails.getAppSolicitorName();
        String ref = nullToEmpty(caseDetails.getApplicantSolicitorRef());
        return new PartySpecificDetails(email, name, ref);
    }
}
