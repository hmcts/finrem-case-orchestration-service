package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyApplicantSolicitorCorresponder;

@Component
@Slf4j
public class IssueApplicationContestedEmailCorresponder extends FinremEmailOnlyApplicantSolicitorCorresponder {

    public IssueApplicationContestedEmailCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        log.info("Sending Contested 'Application Issued' email notification to Applicant Solicitor for caseI {}", caseDetails.getId());
        notificationService.sendContestedApplicationIssuedEmailToApplicantSolicitor(caseDetails);
    }
}
