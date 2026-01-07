package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT;

@Component
public class RespondentPartyListener extends AbstractPartyListener {

    public RespondentPartyListener(BulkPrintService bulkPrintService,
                                  EmailService emailService,
                                  NotificationService notificationService,
                                  InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected boolean isRelevantParty(SendCorrespondenceEvent event) {
        return event.getNotificationParties().contains(NotificationParty.RESPONDENT);
    }

    @Override
    protected boolean isDigitalParty(SendCorrespondenceEvent event) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(event.getCaseDetails());
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        String email = event.getCaseDetails().getRespSolicitorEmail();
        String name = event.getCaseDetails().getRespSolicitorName();
        return new PartySpecificDetails(email, name);
    }

    @Override
    protected CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return bulkPrintService.getRespondentCoverSheet(event.getCaseDetails(), event.authToken);
    }

    @Override
    protected void sendLetter(SendCorrespondenceEvent event,
                              List<BulkPrintDocument> bulkPrintDocs,
                              boolean isOutsideUK) {
        bulkPrintService.bulkPrintFinancialRemedyLetterPack(
            event.caseDetails, RESPONDENT, bulkPrintDocs, isOutsideUK, event.authToken
        );
    }

    @Override
    protected boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return internationalPostalService.isRespondentResideOutsideOfUK(event.getCaseDetails().getData());
    }
}
