package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import com.ibm.icu.text.ListFormatter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
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
    /**
     * The documents to be sent as part of the notification.
     * true = EMAIL
     * false = POSTAL
     */
    @Builder.Default
    List<Boolean> emailOrLetters = new ArrayList<>();

    NotificationRequest emailNotificationRequest;
    EmailTemplateNames emailTemplate;
    List<CaseDocument> documentsToPost;
    FinremCaseDetails caseDetails;
    FinremCaseDetails caseDetailsBefore;
    String authToken;
    Barrister barrister;
    boolean letterNotificationOnly;

    @Setter
    boolean dryRun;
    int letterCount = 0;
    int emailCount = 0;

    private void incrementLetterCount() {
        letterCount++;
    }

    private void incrementEmailCount() {
        emailCount++;
    }

    /**
     * Records that the given notification party would receive, or has received, an email notification.
     *
     * <p>The result is stored in the same index position as the party in {@code notificationParties},
     * so the audit service can later resolve the notification channel for each party.</p>
     *
     * @param notificationParty the party whose notification channel should be recorded
     * @throws IllegalArgumentException if the notification party is null or was not requested on this event
     */
    public void recordEmailNotification(NotificationParty notificationParty) {
        recordNotificationType(notificationParty, true);
        incrementEmailCount();
    }

    /**
     * Records that the given notification party would receive, or has received, a postal notification.
     *
     * <p>The result is stored in the same index position as the party in {@code notificationParties},
     * so the audit service can later resolve the notification channel for each party.</p>
     *
     * @param notificationParty the party whose notification channel should be recorded
     * @throws IllegalArgumentException if the notification party is null or was not requested on this event
     */
    public void recordPostalNotification(NotificationParty notificationParty) {
        recordNotificationType(notificationParty, false);
        incrementLetterCount();
    }

    private void recordNotificationType(NotificationParty notificationParty, boolean email) {
        if (notificationParty == null) {
            throw new IllegalArgumentException("Notification party is required to record notification type");
        }

        int index = getNotificationParties().indexOf(notificationParty);

        if (index == -1) {
            throw new IllegalArgumentException("Notification party was not requested: " + notificationParty);
        }

        while (getEmailOrLetters().size() <= index) {
            getEmailOrLetters().add(null);
        }

        getEmailOrLetters().set(index, email);
    }

    /**
     * Returns the recorded notification type for the given party.
     *
     * <p>This uses the party's index in {@code notificationParties} to read the matching value from
     * {@code emailOrLetters}. A value of {@code true} means email, and {@code false} means postal.</p>
     *
     * @param notificationParty the party whose notification type should be returned
     * @return {@link NotificationType#EMAIL} if the party was recorded as email, otherwise {@link NotificationType#POSTAL}
     * @throws IllegalArgumentException if the notification party was not requested on this event
     * @throws IllegalStateException if no notification type has been recorded for the party
     */
    public NotificationType getNotificationTypeForParty(NotificationParty notificationParty) {
        int index = getNotificationParties().indexOf(notificationParty);

        if (index == -1) {
            throw new IllegalArgumentException("Notification party was not requested: " + notificationParty);
        }

        if (getEmailOrLetters().size() <= index || getEmailOrLetters().get(index) == null) {
            throw new IllegalStateException("No notification type recorded for party: " + notificationParty);
        }

        return Boolean.TRUE.equals(getEmailOrLetters().get(index))
            ? NotificationType.EMAIL
            : NotificationType.POSTAL;
    }

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

    /**
     * Returns a human-readable, comma-separated description of the notification parties.
     *
     * <p>
     * The parties are first mapped to their display labels (e.g. "applicant", "respondent"),
     * then sorted alphabetically, and finally formatted into a natural language list using
     * {@link ListFormatter} for the English locale (e.g. "applicant, respondent and intervener 1").
     *
     * @return a formatted string describing the notification parties; never {@code null}
     * @throws IllegalStateException if an unknown {@link NotificationParty} value is encountered
     */
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
            default -> throw new IllegalStateException("Unable to describe notification party: " + notificationParty);
        };
    }
}
