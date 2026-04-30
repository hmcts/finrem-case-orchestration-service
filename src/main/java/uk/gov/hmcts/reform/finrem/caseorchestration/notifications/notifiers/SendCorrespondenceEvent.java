package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import com.ibm.icu.text.ListFormatter;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    public String describeNotificationParties() {
        return ListFormatter.getInstance(Locale.ENGLISH).format(getNotificationParties()
            .stream().map(this::describeNotificationParty).sorted().toList());
    }

    private String describeNotificationParty(NotificationParty notificationParty) {
        return switch (notificationParty) {
            case APPLICANT -> "applicant";
            case RESPONDENT -> "respondent";
            case INTERVENER_ONE -> "intervener 1";
            case INTERVENER_TWO -> "intervener 2";
            case INTERVENER_THREE -> "intervener 3";
            case INTERVENER_FOUR -> "intervener 4";
            default -> throw new IllegalStateException("Unexpected value: " + notificationParty);
        };
    }
}
