package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.intervener.IntervenerOneToIntervenerDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerChangeDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.interveners.IntervenerDocumentService;

@Component
@Slf4j
public class IntervenerRemovedCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {

    private final IntervenerDocumentService intervenerDocumentService;

    public IntervenerRemovedCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                         IntervenerDocumentService intervenerDocumentService) {
        super(notificationService, bulkPrintService);
        this.intervenerDocumentService = intervenerDocumentService;
    }

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken, IntervenerChangeDetails intervenerChangeDetails) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (intervenerChangeDetails.getIntervenerType().equals(IntervenerChangeDetails.IntervenerType.INTERVENER_ONE)
            && intervenerChangeDetails.getIntervenerAction().equals(IntervenerChangeDetails.IntervenerAction.ADDED)) {

            caseDetails.getData().setCurrentIntervenerChangeDetails(intervenerChangeDetails);
            sendIntervenerOneCorrespondence(caseDetails, authToken);
        }
    }

    protected void sendIntervenerOneCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendIntervenerOneSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to Intervener One for case: {}", caseDetails.getId());
            emailIntervenerSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to Intervener One for case: {}", caseDetails.getId());

            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.INTERVENER_ONE), caseDetails);

            if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                emailApplicantSolicitor(caseDetails);
            }

            if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                emailRespondentSolicitor(caseDetails);
            }

            // TODO: emailApplicantSolicitorIntervenerCitizen

            // TODO: emailRespondentSolicitorIntervenerCitizen
        }
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
        notificationService.sendIntervenerSolicitorAddedEmail(caseDetails,
            new IntervenerOneToIntervenerDetailsMapper().getIntervenerDetails(caseDetails));
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendIntervenerSolicitorRemovedEmail(caseDetails,
            new IntervenerOneToIntervenerDetailsMapper().getIntervenerDetails(caseDetails));
    }

    protected void emailIntervenerSolicitor(FinremCaseDetails caseDetails) {
        notificationService.sendIntervenerSolicitorAddedEmail(caseDetails,
            new IntervenerOneToIntervenerDetailsMapper().getIntervenerDetails(caseDetails));
    }

    protected void emailApplicantSolicitorIntervenerCitizen(FinremCaseDetails caseDetails) {
        notificationService.sendIntervenerCitizenRemovedEmail(caseDetails,
            new IntervenerOneToIntervenerDetailsMapper().getIntervenerDetails(caseDetails));

    }

    protected void emailRespondentSolicitorIntervenerCitizen(FinremCaseDetails caseDetails) {
        notificationService.sendIntervenerCitizenRemovedEmail(caseDetails,
            new IntervenerOneToIntervenerDetailsMapper().getIntervenerDetails(caseDetails));

    }


}
