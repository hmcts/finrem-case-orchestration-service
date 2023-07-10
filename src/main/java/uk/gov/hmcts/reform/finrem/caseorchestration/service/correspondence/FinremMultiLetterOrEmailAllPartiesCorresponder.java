package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremMultiLetterOrEmailAllPartiesCorresponder extends MultiLetterOrEmailAllPartiesCorresponder<FinremCaseDetails> {

    protected final BulkPrintService bulkPrintService;
    protected final NotificationService notificationService;

    protected void sendApplicantCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to applicant for case: {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendRespondentCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending letter correspondence to respondent for case: {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
        }
    }

    public void sendIntervenerCorrespondence(String authorisationToken, FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getData();
        List<IntervenerWrapper> interveners = caseData.getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                log.info("Sending email correspondence to {} for case: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
            } else if (intervenerWrapper.getIntervenerName() != null && !intervenerWrapper.getIntervenerName().isEmpty()) {
                log.info("Sending letter correspondence to {} for case: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken, getDocumentsToPrint(caseDetails));
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

}
