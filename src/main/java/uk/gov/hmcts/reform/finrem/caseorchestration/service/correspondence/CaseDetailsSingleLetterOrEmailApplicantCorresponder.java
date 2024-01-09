package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsSingleLetterOrEmailApplicantCorresponder extends SingleLetterOrEmailApplicantCorresponder<CaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;

    @Override
    @SuppressWarnings("java:S1874")
    public void sendCorrespondence(CaseDetails caseDetails, String authToken) {

        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for Case ID: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for Case ID: {}", caseDetails.getId());
            bulkPrintService.sendDocumentForPrint(getDocumentToPrint(caseDetails, authToken), caseDetails, APPLICANT, authToken);
        }
    }

    public boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    public boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return Boolean.FALSE;
    }

    public abstract CaseDocument getDocumentToPrint(CaseDetails caseDetails, String authorisationToken);

    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);
}
