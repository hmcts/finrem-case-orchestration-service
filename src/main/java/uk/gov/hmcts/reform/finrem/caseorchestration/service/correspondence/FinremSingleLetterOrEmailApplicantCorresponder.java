package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremSingleLetterOrEmailApplicantCorresponder extends SingleLetterOrEmailApplicantCorresponder<FinremCaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;

    @Override
    public void sendCorrespondence(FinremCaseDetails caseDetails, String authToken) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for Case ID: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else if (shouldSendApplicantLetter(caseDetails)) {
            log.info("Sending letter correspondence to applicant for Case ID: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(getDocumentToPrint(caseDetails, authToken), caseDetails, APPLICANT, authToken);
        }
    }

    public boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    public final boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return Boolean.FALSE;
    }

    public abstract CaseDocument getDocumentToPrint(FinremCaseDetails caseDetails, String authorisationToken);

    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    /**
     * Determines whether a letter should be sent to the applicant for the given case.
     *
     * @param caseDetails the case details containing applicant and case information
     * @return {@code true} if the letter should be sent to the applicant; {@code false} otherwise
     */
    public boolean shouldSendApplicantLetter(FinremCaseDetails caseDetails) {
        return true;
    }
}
