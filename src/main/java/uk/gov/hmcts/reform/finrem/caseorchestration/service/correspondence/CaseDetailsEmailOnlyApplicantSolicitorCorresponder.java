package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return Boolean.FALSE;
    }

    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);
}
