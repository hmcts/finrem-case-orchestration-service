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

    protected void sendApplicantCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.APPLICANT), caseDetails, CCDConfigConstant.APPLICANT, authorisationToken);
        }
    }

    protected void sendRespondentCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(
                getDocumentToPrint(
                    caseDetails,
                    authorisationToken,
                    DocumentHelper.PaperNotificationRecipient.RESPONDENT), caseDetails, CCDConfigConstant.RESPONDENT, authorisationToken);
        }
    }

    protected void sendIntervenerCorrespondence(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<IntervenerWrapper> interveners = caseData.getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                log.info("Sending email correspondence to {} for case: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
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

    public abstract CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken,
                                                    DocumentHelper.PaperNotificationRecipient recipient);

    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails);
}
