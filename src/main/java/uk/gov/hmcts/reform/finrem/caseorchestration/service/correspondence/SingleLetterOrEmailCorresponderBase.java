package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class SingleLetterOrEmailCorresponderBase extends CorresponderBase {

    protected final BulkPrintService bulkPrintService;

    @Autowired
    public SingleLetterOrEmailCorresponderBase(NotificationService notificationService,
                                               BulkPrintService bulkPrintService) {
        super(notificationService);
        this.bulkPrintService = bulkPrintService;
    }

    @Override
    public void sendCorrespondence(CaseDetails caseDetails, String authToken) {

        if (shouldSendEmail(caseDetails)) {
            log.info("Sending email correspondence to {} for case: {}", getRecipient(), caseDetails.getId());
            this.emailSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to {} for case: {}", getRecipient(), caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(getDocumentToPrint(caseDetails, authToken, getRecipient()), caseDetails);
        }
    }

    protected abstract DocumentHelper.PaperNotificationRecipient getRecipient();

    public abstract CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);


}
