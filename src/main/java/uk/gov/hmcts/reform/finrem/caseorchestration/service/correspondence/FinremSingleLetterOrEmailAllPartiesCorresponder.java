package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremSingleLetterOrEmailAllPartiesCorresponder extends EmailAndLettersCorresponderBase<FinremCaseDetails> {

    protected final NotificationService notificationService;
    protected final BulkPrintService bulkPrintService;

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
    }

    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails);
        }
    }

    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails);
        }
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    public abstract CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);


    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);
}
