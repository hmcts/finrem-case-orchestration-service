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
public class ConsentOrderAvailableCorresponder extends CaseDetailsEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ConsentOrderAvailableCorresponder(NotificationService notificationService,
                                             FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(notificationService, finremCaseDetailsMapper);
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'", caseDetails.getId());
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        log.info("case - {}: Sending email notification for to Respondent Solicitor for 'Consent Order Available'", caseDetails.getId());
        notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
    }

    @Override
    protected void emailIntervenerSolicitor(IntervenerWrapper intervenerWrapper, CaseDetails caseDetails) {
        log.info("case - {}: Sending email notification for to Intervener Solicitor for 'Consent Order Available'", caseDetails.getId());
        notificationService.sendConsentOrderAvailableEmailToIntervenerSolicitor(caseDetails,
            notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
    }
}
