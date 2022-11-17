package uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf.emails.applicant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf.HwfNotificationsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContestedApplicantHwfEmailHandler implements HwfNotificationsHandler {

    private final CaseDataService caseDataService;
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final NotificationService notificationService;

    @Override
    public void sendNotification(CaseDetails caseDetails, String authToken) {
        log.info("Sending Contested HWF Successful email notification to Solicitor");
        notificationService.sendContestedHwfSuccessfulConfirmationEmail(caseDetails);
    }

    @Override
    public boolean canHandle(CaseDetails caseDetails) {
        return caseDataService.isContestedApplication(caseDetails) && checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }
}
