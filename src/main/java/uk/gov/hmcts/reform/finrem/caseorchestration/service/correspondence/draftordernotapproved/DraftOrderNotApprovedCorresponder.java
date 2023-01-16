package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.draftordernotapproved;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.EmailOnlyAllSolicitorsCorresponder;

@Component
@Slf4j
public class DraftOrderNotApprovedCorresponder extends EmailOnlyAllSolicitorsCorresponder {

    private final CaseDataService caseDataService;

    @Autowired
    public DraftOrderNotApprovedCorresponder(NotificationService notificationService, CaseDataService caseDataService) {
        super(notificationService);
        this.caseDataService = caseDataService;
    }

    @Override
    protected void emailApplicantSolicitor(CaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor for 'Consent Order Not Approved'");
            notificationService.sendConsentOrderNotApprovedEmailToApplicantSolicitor(caseDetails);
        } else {
            log.info("Sending email notification to Applicant Solicitor for 'Contest Order Not Approved'");
            notificationService.sendContestOrderNotApprovedEmailApplicant(caseDetails);
        }
    }

    @Override
    protected void emailRespondentSolicitor(CaseDetails caseDetails) {
        if (caseDataService.isConsentedApplication(caseDetails)) {
            log.info("Sending email notification to Respondent Solicitor for 'Consent Order Not Approved'");
            notificationService.sendConsentOrderNotApprovedEmailToRespondentSolicitor(caseDetails);
        } else {
            log.info("Sending email notification to Respondent Solicitor for 'Contest Order Not Approved'");
            notificationService.sendContestOrderNotApprovedEmailRespondent(caseDetails);
        }
    }
}
