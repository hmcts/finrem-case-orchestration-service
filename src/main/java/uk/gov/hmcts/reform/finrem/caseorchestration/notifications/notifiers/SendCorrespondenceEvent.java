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

import java.util.ArrayList;
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
    boolean letterNotificationOnly;

    public FinremCaseData getCaseData() {
        return Optional.ofNullable(caseDetails)
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    public FinremCaseData getCaseDataBefore() {
        return Optional.ofNullable(caseDetailsBefore)
            .map(FinremCaseDetails::getData)
            .orElse(null);
    }

    public String getCaseId() {
        return Optional.ofNullable(getCaseData())
            .map(FinremCaseData::getCcdCaseId)
            .orElse(null);
    }

    /**
     * Returns the list of notification parties.
     *
     * <p>
     * If the list has not been initialised, it will be created as an empty {@link ArrayList}.
     * This method never returns {@code null}.
     *
     * @return a non-null, mutable list of {@link NotificationParty}
     */
    public List<NotificationParty> getNotificationParties() {
        if (notificationParties == null) {
            this.notificationParties = new ArrayList<>();
        }
        return notificationParties;
    }
}
