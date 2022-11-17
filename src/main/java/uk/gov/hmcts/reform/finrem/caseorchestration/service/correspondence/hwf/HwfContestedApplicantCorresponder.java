package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.EmailOnlyApplicantCorresponder;

@Slf4j
@Component
public class HwfContestedApplicantCorresponder extends EmailOnlyApplicantCorresponder {

    @Autowired
    public HwfContestedApplicantCorresponder(NotificationService notificationService) {
        super(notificationService);
    }

    @Override
    protected void emailApplicant(CaseDetails caseDetails) {
        log.info("Sending Contested HWF Successful email notification to Solicitor");
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(caseDetails);
    }

}
