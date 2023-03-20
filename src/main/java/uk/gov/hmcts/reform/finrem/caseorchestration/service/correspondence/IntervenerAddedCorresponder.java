package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.interveners.IntervenerDocumentService;

@Component
@Slf4j
public class IntervenerAddedCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {

    private final IntervenerDocumentService intervenerDocumentService;

    public IntervenerAddedCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                       IntervenerDocumentService intervenerDocumentService) {
        super(notificationService, bulkPrintService);
        this.intervenerDocumentService = intervenerDocumentService;
    }

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        //check if newly added to send letter to self
        sendIntervenerOneCorrespondence(caseDetails, authToken);
        //check intervener two is
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (caseDetails.getData().isIntervenerOneRepresentedByASolicitor()) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
        } else {
            log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails);
        }
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken,recipient);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {

    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {

    }

}
