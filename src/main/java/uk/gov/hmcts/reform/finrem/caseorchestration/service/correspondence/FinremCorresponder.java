package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@RequiredArgsConstructor
public abstract class FinremCorresponder {

    protected final NotificationService notificationService;

    /**
     * Check if correspondence for an applicant can be emailed to their solicitor.
     *
     * @param caseDetails case details
     * @return true if correspondence for an applicant can be emailed to their solicitor
     */
    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    /**
     * Check if correspondence for a respondent can be emailed to their solicitor.
     *
     * @param caseDetails case details
     * @return true if correspondence for a respondent can be emailed to their solicitor
     */
    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    /**
     * Check if correspondence for an intervener can be emailed to their solicitor.
     *
     * @param caseDetails case details
     * @param intervenerWrapper the intervener details
     * @return true if correspondence for an intervener can be emailed to their solicitor
     */
    protected boolean shouldSendIntervenerSolicitorEmail(FinremCaseDetails caseDetails,
                                                         IntervenerWrapper intervenerWrapper) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }
}
