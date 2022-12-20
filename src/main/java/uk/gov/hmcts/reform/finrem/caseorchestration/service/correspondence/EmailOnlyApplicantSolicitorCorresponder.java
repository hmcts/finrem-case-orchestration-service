package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class EmailOnlyApplicantSolicitorCorresponder extends CorresponderBase {

    @Autowired
    public EmailOnlyApplicantSolicitorCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    public void sendCorrespondence(CaseDetails caseDetails, String authToken) {
        if (shouldSendEmail(caseDetails)) {
            log.info("Sending email correspondence to applicant for case: {}", caseDetails.getId());
            this.emailSolicitor(caseDetails);
        }
    }

    @Override
    protected boolean shouldSendEmail(CaseDetails caseDetails) {
        return notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails);
    }
}
