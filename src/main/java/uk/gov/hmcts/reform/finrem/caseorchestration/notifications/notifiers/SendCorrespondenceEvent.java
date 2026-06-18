package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers;

import com.ibm.icu.text.ListFormatter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationAudit;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.notifications.NotificationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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

    @Builder.Default
    List<NotificationAudit> notificationAudits = new ArrayList<>();

    @Setter
    String eventId;
    /**
     * Indicates whether the correspondence event should run in audit-only mode.
     *
     * <p>When {@code true}, listeners determine which notification would be sent
     * and record the relevant audit row, but they do not send any email or postal
     * notification.</p>
     *
     * <p>When {@code false}, listeners send the actual notification and record the
     * sent audit row.</p>
     */
    @Setter
    boolean dryRun;

    /**
     * Records that the given notification party would receive, or has received, an email notification.
     *
     * @param notificationParty the party whose notification channel should be recorded
     */
    public void recordEmailNotificationToSendAudit(NotificationParty notificationParty) {
        notificationAudits.add(NotificationAudit.builder().createdAt(LocalDate.now())
            .wasSent(YesOrNo.NO)
            .eventId(this.eventId)
            .party(notificationParty.name())
            .type(NotificationType.EMAIL)
            .emailTemplate(this.emailTemplate.name())
            .build());
    }

    public void recordEmailNotificationSentAudit(NotificationParty notificationParty) {
        notificationAudits.add(NotificationAudit.builder().createdAt(LocalDate.now())
            .wasSent(YesOrNo.YES)
            .eventId(this.eventId)
            .party(notificationParty.name())
            .type(NotificationType.EMAIL)
            // Email ID not returned from notify API service calls
            // .emailId(emailId.toString())
            .emailTemplate(this.emailTemplate.name())
            .build());
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
    public void recordPostalNotificationToSendAudit(NotificationParty notificationParty) {
        notificationAudits.add(NotificationAudit.builder().createdAt(LocalDate.now())
            .wasSent(YesOrNo.NO)
            .eventId(this.eventId)
            .party(notificationParty.name())
            .type(NotificationType.POSTAL)
            .attachedPostalDocs(getDocumentsToPostFilenames())
            .build());
    }

    public void recordPostalNotificationSentAudit(NotificationParty notificationParty, UUID letterId) {
        notificationAudits.add(NotificationAudit.builder().createdAt(LocalDate.now())
            .wasSent(YesOrNo.YES)
            .eventId(this.eventId)
            .party(notificationParty.name())
            .type(NotificationType.POSTAL)
            .letterId(letterId.toString())
            .attachedPostalDocs(getDocumentsToPostFilenames())
            .build());
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

    private List<String> getDocumentsToPostFilenames() {
        return Optional.ofNullable(documentsToPost)
            .orElseGet(List::of)
            .stream()
            .map(CaseDocument::getDocumentFilename)
            .filter(fileName -> fileName != null && !fileName.isBlank())
            .toList();
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
