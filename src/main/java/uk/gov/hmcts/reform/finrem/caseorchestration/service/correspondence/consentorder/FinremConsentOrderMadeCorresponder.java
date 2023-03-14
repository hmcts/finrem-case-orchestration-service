package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class FinremConsentOrderMadeCorresponder extends FinremEmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public FinremConsentOrderMadeCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        log.info("Sending email notification to Applicant Solicitor for 'Consent Order Made' for case: {}", caseDetails.getId());
        notificationService.sendConsentOrderMadeConfirmationEmailToApplicantSolicitor(caseDetails);
    }

    @Override
    protected void emailRespondentSolicitor(FinremCaseDetails caseDetails) {
        log.info("Sending email notification to Respondent Solicitor for 'Consent Order Made' for case: {}", caseDetails.getId());
        notificationService.sendConsentOrderMadeConfirmationEmailToRespondentSolicitor(caseDetails);
    }
}


