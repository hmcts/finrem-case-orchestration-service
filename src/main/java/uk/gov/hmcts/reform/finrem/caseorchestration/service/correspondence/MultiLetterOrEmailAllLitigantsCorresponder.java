package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
public abstract class MultiLetterOrEmailAllLitigantsCorresponder extends CorresponderBase {

    protected final BulkPrintService bulkPrintService;

    @Autowired
    public MultiLetterOrEmailAllLitigantsCorresponder(NotificationService notificationService,
                                                      BulkPrintService bulkPrintService) {
        super(notificationService);
        this.bulkPrintService = bulkPrintService;
    }

    public void sendApplicantAndRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        sendApplicantCorrespondence(authorisationToken, caseDetails);
        sendRespondentCorrespondence(authorisationToken, caseDetails);
    }

    public void sendRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondent(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendApplicantCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicant(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public abstract List<BulkPrintDocument> getDocumentsToPrint(CaseDetails caseDetails);

    protected abstract void emailApplicant(CaseDetails caseDetails);

    protected abstract void emailRespondent(CaseDetails caseDetails);
}
