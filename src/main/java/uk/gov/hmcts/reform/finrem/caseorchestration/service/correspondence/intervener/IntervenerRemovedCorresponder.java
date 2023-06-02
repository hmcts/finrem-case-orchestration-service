package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.intervener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
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

        IntervenerChangeDetails intervenerChangeDetails = caseDetails.getData().getCurrentIntervenerChangeDetails();
        log.info("intervener type: {}", intervenerChangeDetails.getIntervenerType());
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_ONE) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerOneWrapper(), caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_TWO) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerTwoWrapper(), caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_THREE) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerThreeWrapper(), caseDetails, authToken);
        } else if (intervenerChangeDetails.getIntervenerType() == IntervenerType.INTERVENER_FOUR) {
            sendIntervenerCorrespondence(caseDetails.getData().getIntervenerFourWrapper(), caseDetails, authToken);
        }
    }

    protected void sendIntervenerCorrespondence(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails, String auth) {

        if (shouldSendIntervenerSolicitorEmail(intervenerWrapper)) {
            log.info("Sending email correspondence to {} for case: {}", intervenerWrapper.getIntervenerType(), caseDetails.getId());
            String recipientName = intervenerWrapper.getIntervenerSolName();
            String recipientEmail = intervenerWrapper.getIntervenerSolEmail();
            String referenceNumber = intervenerWrapper.getIntervenerSolicitorReference();
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerWrapper,
                recipientName, recipientEmail, referenceNumber);
        } else {
            log.info("Sending letter correspondence to {} for case: {}", intervenerWrapper.getIntervenerType(), caseDetails.getId());
            String recipient = intervenerWrapper.getPaperNotificationRecipient().toString();
            caseDetails.getData().getCurrentIntervenerChangeDetails().setIntervenerDetails(
                intervenerWrapper);

            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(caseDetails, auth,
                    intervenerWrapper.getPaperNotificationRecipient()), caseDetails, recipient, auth);
        }
    }

    public CaseDocument getAppRepDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                 DocumentHelper.PaperNotificationRecipient recipient) {
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            return intervenerDocumentService.generateIntervenerSolicitorAddedLetter(caseDetails, authorisationToken, recipient);
        } else {
            return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken, recipient);
        }
    }

    @Override
    public CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                           DocumentHelper.PaperNotificationRecipient recipient) {
        return intervenerDocumentService.generateIntervenerAddedNotificationLetter(caseDetails, authorisationToken, recipient);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper) {
        return notificationService.isIntervenerSolicitorEmailPopulated(intervenerWrapper);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        IntervenerDetails intervenerDetails = caseDetails.getData()
            .getCurrentIntervenerChangeDetails().getIntervenerDetails();
        String recipientName = caseDetails.getData().getAppSolicitorName();
        String recipientEmail = caseDetails.getData().getAppSolicitorEmail();
        String referenceNumber = caseDetails.getData().getContactDetailsWrapper().getSolicitorReference();
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        } else {
            notificationService.sendIntervenerAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        IntervenerDetails intervenerDetails = caseDetails.getData()
            .getCurrentIntervenerChangeDetails().getIntervenerDetails();
        String recipientName = caseDetails.getData().getRespondentSolicitorName();
        String recipientEmail = caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorEmail();
        String referenceNumber = caseDetails.getData().getContactDetailsWrapper().getRespondentSolicitorReference();
        if (caseDetails.getData().getCurrentIntervenerChangeDetails().getIntervenerDetails().getIntervenerRepresented() == YesOrNo.YES) {
            notificationService.sendIntervenerSolicitorAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        } else {
            notificationService.sendIntervenerAddedEmail(caseDetails, intervenerDetails, recipientName, recipientEmail, referenceNumber);
        }
    }
}
