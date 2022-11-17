package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class SingleLetterAndEmailApplicantCorresponder extends CorresponderBase {

    protected final BulkPrintService bulkPrintService;

    @Autowired
    public SingleLetterAndEmailApplicantCorresponder(NotificationService notificationService,
                                                     BulkPrintService bulkPrintService) {
        super(notificationService);
        this.bulkPrintService = bulkPrintService;
    }

    public void sendApplicantCorrespondence(String authorisationToken, CaseDetails caseDetails) {

        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicant(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(getDocumentToPrint(caseDetails, authorisationToken), caseDetails);
        }
    }

    public abstract CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken);

    protected abstract void emailApplicant(CaseDetails caseDetails);


}
