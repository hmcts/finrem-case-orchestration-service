package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class EmailOnlyApplicantCorresponder extends CorresponderBase {

    @Autowired
    public EmailOnlyApplicantCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    public void sendApplicantEmail(CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicant(caseDetails);
        }
    }

    protected abstract void emailApplicant(CaseDetails caseDetails);

}
