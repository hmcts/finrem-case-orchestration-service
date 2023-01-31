package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.EmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class ConsentOrderNotApprovedSentCorresponder extends EmailOnlyAllSolicitorsCorresponder {

    @Autowired
    public ConsentOrderNotApprovedSentCorresponder(NotificationService notificationService) {
        super(notificationService);
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
}
