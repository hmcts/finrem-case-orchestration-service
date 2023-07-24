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
public class ContestedIntermHearingCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ContestedIntermHearingCorresponder(NotificationService notificationService,
                                              FinremCaseDetailsMapper firemCaseDetailsMapper) {
        super(notificationService, firemCaseDetailsMapper);
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        if (notificationService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'interim hearing' for case: {}", caseDetails.getId());
            notificationService.sendInterimNotificationEmailToApplicantSolicitor(caseDetails);
        }
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        if (notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getData())) {
            log.info("Sending email notification to Respondent Solicitor for 'interim hearing' for case: {}", caseDetails.getId());
            notificationService.sendInterimNotificationEmailToRespondentSolicitor(caseDetails);
        }
    }

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("Sending email notification to Intervener Solicitor for 'interim hearing' for case: {}", caseDetails.getId());
        notificationService.sendInterimNotificationEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }
}
