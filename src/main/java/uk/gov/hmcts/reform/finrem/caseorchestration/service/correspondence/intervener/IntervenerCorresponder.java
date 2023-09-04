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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremSingleLetterOrEmailAllPartiesCorresponder;

@Slf4j
@Component
public class IntervenerCorresponder extends FinremSingleLetterOrEmailAllPartiesCorresponder {

    protected final IntervenerDocumentService intervenerDocumentService;

    public IntervenerCorresponder(NotificationService notificationService, BulkPrintService bulkPrintService,
                                  IntervenerDocumentService intervenerDocumentService) {
        super(notificationService, bulkPrintService);
        this.intervenerDocumentService = intervenerDocumentService;
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

    @Override
    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String auth) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.APPLICANT.toString();
            bulkPrintService.sendDocumentForPrint(
                getAppRepDocumentToPrint(caseDetails, auth,
                    DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails, recipient, auth);
        }
    }

    @Override
    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String auth) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            String recipient = DocumentHelper.PaperNotificationRecipient.RESPONDENT.toString();
            bulkPrintService.sendDocumentForPrint(
                getAppRepDocumentToPrint(caseDetails, auth,
                    DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails, recipient, auth);
        }
    }

    @Override
    protected boolean shouldSendIntervenerLetter(IntervenerWrapper intervenerWrapper) {
        return intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty();
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

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper) {
        return notificationService.wasIntervenerSolicitorEmailPopulated(intervenerWrapper);
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

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        log.info("require refactor to use this method");
    }
}
