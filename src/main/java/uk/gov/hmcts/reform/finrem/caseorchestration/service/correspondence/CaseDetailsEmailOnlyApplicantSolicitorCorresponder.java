package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsEmailOnlyApplicantSolicitorCorresponder extends EmailOnlyCorresponderBase<CaseDetails> {

    protected final NotificationService notificationService;

    @Override
    public void sendCorrespondence(CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        }
    }

    @Override
    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    @Override
    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return Boolean.FALSE;
    }

    @Override
    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        return false;
    }

    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);
}
