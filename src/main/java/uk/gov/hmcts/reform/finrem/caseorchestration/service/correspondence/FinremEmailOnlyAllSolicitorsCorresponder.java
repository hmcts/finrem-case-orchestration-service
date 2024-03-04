package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class FinremEmailOnlyAllSolicitorsCorresponder extends EmailOnlyCorresponderBase<FinremCaseDetails> {

    protected final NotificationService notificationService;

    @Override
    public void sendCorrespondence(FinremCaseDetails caseDetails) {
        log.info("Determine whether to send email notifications to all solicitors for Case ID: {}", caseDetails.getId());
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for Case ID: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        }
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for Case ID: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        }
    }

    protected boolean shouldSendApplicantSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(FinremCaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean isEmailToIntervenerSolicitorRequired(FinremCaseDetails caseDetails,
                                                           IntervenerWrapper intervenerWrapper) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);

}
