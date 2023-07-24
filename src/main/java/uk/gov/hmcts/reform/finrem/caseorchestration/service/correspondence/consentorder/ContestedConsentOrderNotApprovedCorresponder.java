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
public class ContestedConsentOrderNotApprovedCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ContestedConsentOrderNotApprovedCorresponder(NotificationService notificationService,
                                                        FinremCaseDetailsMapper caseDetailsMapper) {
        super(notificationService, caseDetailsMapper);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor for 'Contested Consent Order Not Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestedConsentOrderNotApprovedEmailApplicantSolicitor(caseDetails);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor for 'Contested Consent Order Not Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestedConsentOrderNotApprovedEmailRespondentSolicitor(caseDetails);

    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("Sending email notification to Intervener Solicitor for 'Contested Consent Order Not Approved' for case: {}", caseDetails.getId());
        notificationService.sendContestedConsentOrderNotApprovedEmailIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));

    }
}
