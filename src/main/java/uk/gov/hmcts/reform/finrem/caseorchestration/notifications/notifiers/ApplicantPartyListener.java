package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@Component
public class ApplicantPartyListener extends AbstractPartyListener {

    public ApplicantPartyListener(BulkPrintService bulkPrintService,
                                  EmailService emailService,
                                  NotificationService notificationService,
                                  InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
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
    protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return bulkPrintService.getApplicantCoverSheet(event.getCaseDetails(), event.authToken);
    }

    @Override
    protected void sendLetter(SendCorrespondenceEvent event, List<BulkPrintDocument> bpDocs, Boolean isOutsideUK) {
        bulkPrintService.bulkPrintFinancialRemedyLetterPack(event.caseDetails, APPLICANT, bpDocs, isOutsideUK,  event.authToken);
    }

    @Override
    protected Boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return internationalPostalService.isApplicantResideOutsideOfUK(event.getCaseDetails().getData());
    }
}
