package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

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
        if (caseDetails.isContestedApplication()) {
            final List<IntervenerWrapper> interveners =  caseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper -> {
                if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                    log.info("Sending email correspondence to {} for Case ID: {}",
                        intervenerWrapper.getIntervenerType().getTypeValue(),
                        caseDetails.getId());
                    this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
                }
            });
        }
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

    protected abstract void emailApplicantSolicitor(FinremCaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(FinremCaseDetails caseDetails);

    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
