package uk.gov.hmcts.reform.finrem.caseorchestration.service.hwf;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

public interface HwfNotificationsHandler {

    void sendNotification(CaseDetails caseDetails, String authToken);

    boolean canHandle(CaseDetails caseDetails);
}
