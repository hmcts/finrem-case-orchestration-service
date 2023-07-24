package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.CaseDetailsEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class ConsentOrderNotApprovedCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    private final CaseDataService caseDataService;

    @Autowired
    public ConsentOrderNotApprovedCorresponder(NotificationService notificationService,
                                               CaseDataService caseDataService,
                                               FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(notificationService, finremCaseDetailsMapper);
        this.caseDataService = caseDataService;
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Not Approved' for case: {}", caseDetails.getId());
            notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending email notification to Applicant Solicitor for 'Contest Order Not Approved' for case:{}", caseDetails.getId());
            notificationService.sendContestOrderNotApprovedEmailApplicant(caseDetails);
        }
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Not Approved' for case: {}", caseDetails.getId());
            notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending email notification to Respondent Solicitor for 'Contest Order Not Approved' for case: {}", caseDetails.getId());
            notificationService.sendContestOrderNotApprovedEmailRespondent(caseDetails);
        }
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        if (caseDataService.isContestedApplication(caseDetails)) {
            log.info("Sending email notification to Intervener Solicitor for 'Contest Order Not Approved' for case: {}", caseDetails.getId());
            notificationService.sendContestOrderNotApprovedEmailIntervener(caseDetails,
                notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
        }
    }
}
