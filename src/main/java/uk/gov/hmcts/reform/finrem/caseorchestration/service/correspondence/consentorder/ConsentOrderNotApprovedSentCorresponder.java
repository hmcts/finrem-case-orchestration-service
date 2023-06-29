package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class ConsentOrderNotApprovedSentCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ConsentOrderNotApprovedSentCorresponder(NotificationService notificationService,
                                                   FinremCaseDetailsMapper firemCaseDetailsMapper) {
        super(notificationService, firemCaseDetailsMapper);
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor about consent order not approved being sent for case: {}", caseDetails.getId());
        notificationService.sendConsentOrderNotApprovedSentEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor about consent order not approved being sent for case: {}", caseDetails.getId());
        notificationService.sendConsentOrderNotApprovedSentEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("Sending email notification to Intervener Solicitor about consent order not approved being sent for case: {}", caseDetails.getId());
        notificationService.sendConsentOrderNotApprovedSentEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }
}
