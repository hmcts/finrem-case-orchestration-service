package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class EmailOnlyApplicantSolicitorCorresponder extends EmailOnlyCorresponderBase  {

    @Autowired
    public EmailOnlyApplicantSolicitorCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    public void sendCorrespondence(CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicantSolicitor(caseDetails);
        }
    }

    protected abstract void emailApplicantSolicitor(CaseDetails caseDetails);
}
