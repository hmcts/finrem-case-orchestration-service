package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@RequiredArgsConstructor
public abstract class FinremCorresponder {

    protected final NotificationService notificationService;

    protected boolean isApplicantCorrespondenceEnabled(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isApplicantCorrespondenceEnabled();
    }

    protected boolean isRespondentCorrespondenceEnabled(FinremCaseDetails caseDetails) {
        return caseDetails.getData().isRespondentCorrespondenceEnabled();
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }
}
