package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationsApplicantRejectionEmailOrLetterHandler {


    private final NotificationService notificationService;
    private final PaperNotificationService paperNotificationService;

    public void sendApplicantNotifications(String userAuthorisation, CaseDetails caseDetails) {
        if (notificationService.isApplicantSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            notificationService.sendGeneralApplicationRejectionEmailToAppSolicitor(caseDetails);
        } else {
            paperNotificationService.printApplicantRejectionGeneralApplication(caseDetails, userAuthorisation);
        }
    }
}
