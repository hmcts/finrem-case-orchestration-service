package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

public abstract class EmailNotificationOnlyListener extends AbstractPartyListener {

    protected EmailNotificationOnlyListener(BulkPrintService bulkPrintService,
                                            EmailService emailService,
                                            NotificationService notificationService,
                                            InternationalPostalService internationalPostalService) {
        super(bulkPrintService, emailService, notificationService, internationalPostalService);
    }

    @Override
    protected final CaseDocument getPartyCoversheet(SendCorrespondenceEvent event) {
        return null;
    }

    @Override
    protected final boolean isPartyOutsideUK(SendCorrespondenceEvent event) {
        return false;
    }

    @Override
    protected final boolean shouldSendPaperNotification(SendCorrespondenceEvent event) {
        return false;
    }

    @Override
    protected PartySpecificDetails setPartySpecificDetails(SendCorrespondenceEvent event) {
        return null;
    }
}
