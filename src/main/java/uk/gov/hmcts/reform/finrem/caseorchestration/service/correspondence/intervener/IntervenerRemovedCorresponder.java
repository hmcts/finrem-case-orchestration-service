package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Component
public class IntervenerRemovedCorresponder extends IntervenerCorresponder {

    public IntervenerRemovedCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                         IntervenerDocumentService intervenerDocumentService) {
        super(notificationService, bulkPrintService, intervenerDocumentService);
    }

    @Override
    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        IntervenerChangeDetails intervenerChangeDetails = caseDetails.getData().getCurrentIntervenerChangeDetails();
        if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_ONE) {
            sendIntervenerOneCorrespondence(caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_TWO) {
            sendIntervenerTwoCorrespondence(caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_THREE) {
            sendIntervenerThreeCorrespondence(caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_FOUR) {
            sendIntervenerFourCorrespondence(caseDetails, authToken);
        }
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails, recipient);
        }
    }

    protected void sendIntervenerTwoCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Two for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener Two for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_TWO), caseDetails, recipient);
        }
    }

    protected void sendIntervenerThreeCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Three for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener Three for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_THREE), caseDetails, recipient);
        }
    }

    protected void sendIntervenerFourCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener Four for case: {}", caseDetails.getId());
            //send email
        } else {
            log.info("Sending letter correspondence to Intervener Four for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR.toString();
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_FOUR), caseDetails, recipient);
        }
    }

    @Override
    public CaseDocument getAppRepDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            return intervenerDocumentService.generateIntervenerSolicitorRemovedLetter(caseDetails, authorisationToken, recipient);
        } else {
            return intervenerDocumentService.generateIntervenerRemovedNotificationLetter(caseDetails, authorisationToken, recipient);
        }
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                 DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerRemovedNotificationLetter(caseDetails, authorisationToken, recipient);
    }

    @Override
    protected boolean shouldSendIntervenerSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.wasIntervenerSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        // TODO document why this method is empty
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        // TODO document why this method is empty
    }

}
