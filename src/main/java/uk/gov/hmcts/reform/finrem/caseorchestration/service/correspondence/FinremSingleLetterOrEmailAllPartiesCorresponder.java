package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremSingleLetterOrEmailAllPartiesCorresponder extends EmailAndLettersCorresponderBase<FinremCaseDetails> {

    protected final NotificationService notificationService;

    protected final BulkPrintService bulkPrintService;

    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        sendApplicantCorrespondence(caseDetails, authToken);
        sendRespondentCorrespondence(caseDetails, authToken);
        if (caseDetails.isContestedApplication()) {
            sendIntervenerCorrespondence(caseDetails, authToken);
        }
    }

    public abstract CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);

    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for Case ID: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else if (shouldSendApplicantLetter(caseDetails)) {
            log.info("Sending letter correspondence to applicant for Case ID: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails, CCDConfigConstant.APPLICANT, authorisationToken);
        } else {
            log.info("Nothing is sent to applicant for Case ID: {}", caseDetails.getId());
        }
    }

    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for Case ID: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else if (shouldSendRespondentLetter(caseDetails)) {
            log.info("Sending letter correspondence to respondent for Case ID: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails, CCDConfigConstant.RESPONDENT, authorisationToken);
        } else {
            log.info("Nothing is sent to respondent for Case ID: {}", caseDetails.getId());
        }
    }

    protected void sendIntervenerCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<IntervenerWrapper> interveners = caseData.getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                log.info("Sending email correspondence to {} for Case ID: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
            } else if (shouldSendIntervenerLetter(intervenerWrapper)) {
                log.info("Sending letter correspondence to {} for Case ID: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                bulkPrintService.sendDocumentForPrint(
                    getDocumentToPrint(
                        caseDetails,
                        authorisationToken,
                        intervenerWrapper.getPaperNotificationRecipient()), caseDetails,
                    intervenerWrapper.getIntervenerType().getTypeValue(), authorisationToken);
            } else {
                log.info("Nothing is sent to intervener for Case ID: {}", caseDetails.getId());
            }
        });
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

    protected boolean shouldSendApplicantLetter(FinremCaseDetails caseDetails) {
        return true;
    }

    protected boolean shouldSendRespondentLetter(FinremCaseDetails caseDetails) {
        return true;
    }

    protected abstract boolean shouldSendIntervenerLetter(IntervenerWrapper intervenerWrapper);

    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails);

}
