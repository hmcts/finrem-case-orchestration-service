package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class EmailOnlyAllLitigantsCorresponder extends CorresponderBase {

    @Autowired
    public EmailOnlyAllLitigantsCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    public void sendEmails(CaseDetails caseDetails) {
        if (shouldSendApplicantSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailApplicant(caseDetails);
        }
        if (shouldSendRespondentSolicitorEmail(caseDetails)) {
            log.info("Sending email correspondence to respondent for case: {}", caseDetails.getId());
            this.emailRespondent(caseDetails);
        }
    }

    protected abstract void emailApplicant(CaseDetails caseDetails);

    protected abstract void emailRespondent(CaseDetails caseDetails);
}
