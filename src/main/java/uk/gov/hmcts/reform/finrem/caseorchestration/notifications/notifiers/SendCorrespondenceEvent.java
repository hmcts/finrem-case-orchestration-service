package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;

@Getter
@Builder
public class SendCorrespondenceEvent {
    List<NotificationParty> notificationParties;
    NotificationRequest emailNotificationRequest;
    EmailTemplateNames emailTemplateId;
    List<CaseDocument> documentsToPost;
    FinremCaseDetails caseDetails;
    String authToken;
}
