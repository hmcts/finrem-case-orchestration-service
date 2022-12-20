package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class CorresponderBase {

    protected final NotificationService notificationService;

    public abstract void sendCorrespondence(CaseDetails caseDetails, String authToken);


    protected abstract boolean shouldSendEmail(CaseDetails caseDetails);


    protected abstract void emailSolicitor(CaseDetails caseDetails);

}
