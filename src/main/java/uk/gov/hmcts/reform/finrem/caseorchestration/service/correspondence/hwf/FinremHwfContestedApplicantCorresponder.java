package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.FinremEmailOnlyApplicantSolicitorCorresponder;

@Slf4j
@Component
public class FinremHwfContestedApplicantCorresponder extends FinremEmailOnlyApplicantSolicitorCorresponder {

    @Autowired
    public FinremHwfContestedApplicantCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    public void emailApplicantSolicitor(FinremCaseDetails caseDetails) {
        log.info("Sending Contested HWF Successful email notification to Solicitor");
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(caseDetails);
    }

}
