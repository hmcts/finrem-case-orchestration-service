package uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf.letters.applicant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf.HwfNotificationsHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicantHwfLetterHandler implements HwfNotificationsHandler {

    private CaseDataService caseDataService;
    private CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private PaperNotificationService paperNotificationService;

    @Override
    public void sendNotification(CaseDetails caseDetails, String authToken) {
        paperNotificationService.printHwfSuccessfulNotification(caseDetails, authToken);
    }

    @Override
    public boolean canHandle(CaseDetails caseDetails) {
        return caseDataService.isConsentedApplication(caseDetails) && !checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails);
    }
}
