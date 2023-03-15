package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class FinremConsentOrderAvailableCorresponder extends FinremEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public FinremConsentOrderAvailableCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        log.info("case - {}: Sending email notification for to Applicant Solicitor for 'Consent Order Available'", caseDetails.getId());
        notificationService.sendConsentOrderAvailableEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        log.info("case - {}: Sending email notification for to Respondent Solicitor for 'Consent Order Available'", caseDetails.getId());
        notificationService.sendConsentOrderAvailableEmailToRespondentSolicitor(caseDetails);
    }
}
