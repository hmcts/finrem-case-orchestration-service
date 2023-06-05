package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public abstract class CaseDetailsEmailOnlyAllSolicitorsCorresponder extends EmailOnlyCorresponderBase<CaseDetails> {

    protected final NotificationService notificationService;
    protected final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Override
    public void sendCorrespondence(CaseDetails caseDetails) {
        log.info("Determine whether to send email notifications to all solicitors for case: {}", caseDetails.getId());
        sendApplicantCorrespondence(caseDetails);
        sendRespondentCorrespondence(caseDetails);
        sendIntervenerCorrespondence(caseDetails);
    }

    private void sendApplicantCorrespondence(CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        }
    }

    private void sendRespondentCorrespondence(CaseDetails caseDetails) {
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondentSolicitor(caseDetails);
        }
    }

    private void sendIntervenerCorrespondence(CaseDetails caseDetails) {
        final FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        final List<IntervenerWrapper> interveners =  finremCaseDetails.getData().getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (shouldSendIntervenerSolicitorEmail(intervenerWrapper, caseDetails)) {
                log.info("Sending email correspondence to {} for case: {}",
                    intervenerWrapper.getIntervenerType().getTypeValue(),
                    caseDetails.getId());
                this.emailIntervenerSolicitor(intervenerWrapper, caseDetails);
            }
        });
    }

    protected boolean shouldSendApplicantSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendRespondentSolicitorEmail(CaseDetails caseDetails) {
        return notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails);
    }

    protected boolean shouldSendIntervenerSolicitorEmail(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        return notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails);
    }

    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);

    protected abstract void emailRespondentSolicitor(CaseDetails caseDetails);

    protected abstract void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails);

}
