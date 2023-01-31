package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@Slf4j
public abstract class EmailAndLettersCorresponderBase extends CorresponderBase {

    public EmailAndLettersCorresponderBase(NotificationService notificationService) {
        super(notificationService);
    }

    public abstract void sendCorrespondence(CaseDetails caseDetails, String authToken);

}
