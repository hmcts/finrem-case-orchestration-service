package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.intervener.IntervenerType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.List;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
public class SendCorrespondenceEvent {
    List<NotificationParty> notificationParties;
    NotificationRequest emailNotificationRequest;
    EmailTemplateNames emailTemplate;
    List<CaseDocument> documentsToPost;
    FinremCaseDetails caseDetails;
    FinremCaseDetails caseDetailsBefore;
    String authToken;
    IntervenerType intervenerType;
    Barrister barrister;

    public FinremCaseData getCaseData() {
        return Optional.ofNullable(caseDetails)
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    public FinremCaseData getCaseDataBefore() {
        return caseDetailsBefore.getData();
    }

    public String getCaseId() {
        return Optional.ofNullable(getCaseData())
            .map(FinremCaseData::getCcdCaseId)
            .orElse(null);
    }
}
