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
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
public class SendCorrespondenceEvent {
    List<NotificationParty> notificationParties;

    @Builder.Default
    Map<NotificationParty, RecordedNotification> recordedNotificationsByParty =
        new EnumMap<>(NotificationParty.class);

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

    public record RecordedNotification(
        NotificationType type,
        String letterId
    ) {
        public static RecordedNotification email() {
            return new RecordedNotification(NotificationType.EMAIL, null);
        }

        public static RecordedNotification postal(UUID letterId) {
            return new RecordedNotification(
                NotificationType.POSTAL,
                letterId == null ? null : letterId.toString()
            );
        }
    }

    /**
     * Records that the given notification party would receive, or has received, an email notification.
     *
     * @param notificationParty the party whose notification channel should be recorded
     */
    public void recordEmailNotification(NotificationParty notificationParty) {
        recordNotification(notificationParty, RecordedNotification.email());
    }

    /**
     * Records that the given notification party would receive, or has received, a postal notification.
     *
     * <p>
     * This is used during dry-run audit creation, where no real Bulk Print letter ID exists yet.
     * </p>
     *
     * @param notificationParty the party whose notification channel should be recorded
     */
    public void recordPostalNotification(NotificationParty notificationParty) {
        recordPostalNotification(notificationParty, null);
    }

    /**
     * Records that the given notification party has received a postal notification.
     *
     * <p>
     * This is used during the submitted callback after Bulk Print returns a real letter ID.
     * </p>
     *
     * @param notificationParty the party whose notification channel should be recorded
     * @param letterId the Bulk Print letter ID
     */
    public void recordPostalNotification(NotificationParty notificationParty, UUID letterId) {
        recordNotification(notificationParty, RecordedNotification.postal(letterId));
    }

    private void recordNotification(NotificationParty notificationParty,
                                    RecordedNotification recordedNotification) {
        getRecordedNotificationsByParty().put(notificationParty, recordedNotification);
    }

    /**
     * Returns the recorded notification type for the given party.
     *
     * @param notificationParty the party whose notification type should be returned
     * @return {@link NotificationType#EMAIL} or {@link NotificationType#POSTAL}
     */
    public NotificationType getNotificationTypeForParty(NotificationParty notificationParty) {
        return getRecordedNotificationForParty(notificationParty).type();
    }

    /**
     * Returns the full recorded notification result for the given party.
     *
     * @param notificationParty the party whose recorded notification should be returned
     * @return the recorded notification details
     * @throws IllegalStateException if no notification has been recorded for the party
     */
    public RecordedNotification getRecordedNotificationForParty(NotificationParty notificationParty) {
        RecordedNotification recordedNotification = getRecordedNotificationsByParty().get(notificationParty);

        if (recordedNotification == null) {
            throw new IllegalStateException("No notification recorded for party: " + notificationParty);
        }

        return recordedNotification;
    }

    public Map<NotificationParty, RecordedNotification> getRecordedNotificationsByParty() {
        if (recordedNotificationsByParty == null) {
            recordedNotificationsByParty = new EnumMap<>(NotificationParty.class);
        }
        return recordedNotificationsByParty;
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
