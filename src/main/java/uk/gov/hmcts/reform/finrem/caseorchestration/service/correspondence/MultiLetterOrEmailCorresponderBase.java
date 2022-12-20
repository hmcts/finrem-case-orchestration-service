package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
public abstract class MultiLetterOrEmailCorresponderBase extends CorresponderBase {

    protected final BulkPrintService bulkPrintService;

    @Autowired
    public MultiLetterOrEmailCorresponderBase(NotificationService notificationService,
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
            log.info("Sending multi letter correspondence to {} for case: {}", getRecipient(), caseDetails.getId());
            if (DocumentHelper.PaperNotificationRecipient.APPLICANT.equals(getRecipient())) {
                bulkPrintService.printApplicantDocuments(caseDetails, authToken, getDocumentsToPrint(caseDetails));
            } else if (DocumentHelper.PaperNotificationRecipient.RESPONDENT.equals(getRecipient())) {
                bulkPrintService.printRespondentDocuments(caseDetails, authToken, getDocumentsToPrint(caseDetails));
            }
        }
    }

    protected abstract DocumentHelper.PaperNotificationRecipient getRecipient();

    protected abstract List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails);

}
