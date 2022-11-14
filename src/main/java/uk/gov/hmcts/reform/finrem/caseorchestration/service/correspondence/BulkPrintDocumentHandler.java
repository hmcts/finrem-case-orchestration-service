package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class BulkPrintDocumentHandler {

    private final BulkPrintService bulkPrintService;
    private final NotificationService notificationService;

    public void sendToBulkPrint(String authorisationToken, CaseDetails caseDetails) {

        if (!notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
        if (!notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    abstract List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails);
}
