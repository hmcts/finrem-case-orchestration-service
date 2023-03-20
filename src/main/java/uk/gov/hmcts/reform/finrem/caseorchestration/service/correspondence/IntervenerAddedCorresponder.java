package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
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

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken, IntervenerChangeDetails intervenerChangeDetails) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (intervenerChangeDetails.getIntervenerType().equals(IntervenerChangeDetails.IntervenerType.INTERVENER_ONE) &&
            intervenerChangeDetails.getIntervenerAction().equals(IntervenerChangeDetails.IntervenerAction.ADDED)) {
            sendIntervenerOneCorrespondence(caseDetails, authToken);
        }


    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerOneSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails);
        } //map to generic intervener object 
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken,recipient);
    }

    protected boolean shouldSendIntervenerOneSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerOneSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {

    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {

    }

}
