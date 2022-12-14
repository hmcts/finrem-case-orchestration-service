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
public abstract class SingleLetterOrEmailAllLitigantsCorresponder extends CorresponderBase {

    protected final BulkPrintService bulkPrintService;

    @Autowired
    public SingleLetterOrEmailAllLitigantsCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService) {
        super(notificationService);
        this.bulkPrintService = bulkPrintService;
    }

    public void sendApplicantAndRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        sendApplicantCorrespondence(authorisationToken, caseDetails);
        sendRespondentCorrespondence(authorisationToken, caseDetails);
    }

    protected void sendApplicantCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicant(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, DocumentHelper.PaperNotificationRecipient.APPLICANT, authorisationToken), caseDetails);
        }
    }


    protected void sendRespondentCorrespondence(String authorisationToken, CaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondent(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, DocumentHelper.PaperNotificationRecipient.RESPONDENT, authorisationToken), caseDetails);
        }
    }


    public abstract CaseDocument getDocumentToPrint(CaseDetails caseDetails, DocumentHelper.PaperNotificationRecipient recipient,
                                                    String authorisationToken);

    protected abstract void emailApplicant(CaseDetails caseDetails);

    protected abstract void emailRespondent(CaseDetails caseDetails);
}
