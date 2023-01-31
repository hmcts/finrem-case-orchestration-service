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
public abstract class SingleLetterOrEmailAllPartiesCorresponder extends EmailAndLettersCorresponderBase {

    protected final BulkPrintService bulkPrintService;

    @Autowired
    public SingleLetterOrEmailAllPartiesCorresponder(NotificationService notificationService,
                                                     BulkPrintService bulkPrintService) {
        super(notificationService);
        this.bulkPrintService = bulkPrintService;
    }

    public void sendCorrespondence(CaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
    }

    protected void sendApplicantCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails);
        }
    }

    protected void sendRespondentCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails);
        }
    }

    public abstract CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);


    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(CaseDetails caseDetails);
}
