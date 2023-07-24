package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateFrcCorrespondenceService {

    private final UpdateFrcLetterOrEmailAllSolicitorsCorresponder updateFrcLetterOrEmailAllSolicitorsCorresponder;
    private final NotificationService notificationService;

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void sendCorrespondence(CaseDetails caseDetails, String authToken) throws JsonProcessingException {
        log.info("Send Update FRC correspondence for case: {}", caseDetails.getId());

        updateFrcLetterOrEmailAllSolicitorsCorresponder.sendCorrespondence(caseDetails, authToken);

        log.info("Sending email notification to court for 'Update Frc Information' for case: {}", caseDetails.getId());
        notificationService.sendUpdateFrcInformationEmailToCourt(caseDetails);

    }
}
