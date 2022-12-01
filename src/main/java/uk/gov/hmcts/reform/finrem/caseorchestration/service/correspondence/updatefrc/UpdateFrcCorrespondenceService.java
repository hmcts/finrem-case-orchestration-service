package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaperNotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateFrcCorrespondenceService {

    private final UpdateFrcEmailAllLitigantsCorresponder updateFrcEmailAllLitigantsCorresponder;
    private final NotificationService notificationService;
    private final PaperNotificationService paperNotificationService;

    public void sendCorrespondence(CaseDetails caseDetails, String authToken) throws JsonProcessingException {
        log.info("Send Update FRC correspondence for case: {}", caseDetails.getId());

        updateFrcEmailAllLitigantsCorresponder.sendEmails(caseDetails);

        log.info("Sending email notification to court for 'Update Frc Information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToCourt(caseDetails);

        log.info("Sending letter notification to court for 'Update Frc Information' for case: {}", caseDetails.getId());
        paperNotificationService.printUpdateFrcInformationNotification(caseDetails, authToken);
        
    }
}
