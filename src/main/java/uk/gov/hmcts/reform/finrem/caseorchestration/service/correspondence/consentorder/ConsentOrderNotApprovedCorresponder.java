package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class ConsentOrderNotApprovedCorresponder extends FinremEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ConsentOrderNotApprovedCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        if (caseDetails.isConsentedApplication()) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Not Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending email notification to Applicant Solicitor for 'Contest Order Not Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendContestOrderNotApprovedEmailApplicant(caseDetails);
        }
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        if (caseDetails.isConsentedApplication()) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Not Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending email notification to Respondent Solicitor for 'Contest Order Not Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendContestOrderNotApprovedEmailRespondent(caseDetails);
        }
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, FinremCaseDetails caseDetails) {
        if (caseDetails.isContestedApplication()) {
            log.info("Sending email notification to Intervener Solicitor for 'Contest Order Not Approved' for Case ID: {}", caseDetails.getId());
            notificationService.sendContestOrderNotApprovedEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
        }
    }
}
