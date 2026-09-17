package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Maps CUI document upload data into a notification request.
 *
 * <p>Recipient details are resolved by {@link NotificationParty}. If the selected party has no
 * email address, this mapper returns {@link Optional#empty()} so handlers can skip publishing.
 */
@Service
public class CUINotificationRequestMapper {

    private static final DateTimeFormatter TIME_OF_SUBMISSION_FORMAT = DateTimeFormatter.ofPattern("h:mma 'on' dd/MM/yyyy");

    /**
     * Builds a notification request for the CUI applicant/respondent document upload confirmation email.
     *
     * @param caseDetails finrem case details
     * @param notificationParty target notification party
     * @return populated notification request when recipient email exists; otherwise empty
     */
    public Optional<NotificationRequest> build(FinremCaseDetails caseDetails, NotificationParty notificationParty) {
        Recipient recipient = getRecipient(caseDetails, notificationParty);

        if (!StringUtils.hasText(recipient.email())) {
            return Optional.empty();
        }

        return Optional.of(NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getCaseIdAsString())
            .name(recipient.name())
            .notificationEmail(recipient.email())
            .caseType(CaseType.CONTESTED.name().toLowerCase())
            .contactCourtName(getCitizenUploadCourtName(caseDetails))
            .contactCourtEmail(getCitizenUploadCourtEmail(caseDetails))
            .timeOfSubmission(getCitizenUploadTime())
            .build());
    }

    /**
     * Resolves citizen name and email address for the target notification party.
     */
    private Recipient getRecipient(FinremCaseDetails caseDetails, NotificationParty notificationParty) {
        return switch (notificationParty) {
            case CUI_APPLICANT -> new Recipient(
                caseDetails.getData().getFullApplicantName(),
                caseDetails.getData().getContactDetailsWrapper().getApplicantEmail()
            );
            case CUI_RESPONDENT -> new Recipient(
                caseDetails.getData().getRespondentFullName(),
                caseDetails.getData().getContactDetailsWrapper().getRespondentEmail()
            );
            default -> throw new IllegalStateException("Unsupported notification party: " + notificationParty);
        };
    }

    /**
     * Returns the FRC name for first in-person hearing, otherwise empty string.
     */
    private String getCitizenUploadCourtName(FinremCaseDetails caseDetails) {
        if (!isFirstHearingInPerson(caseDetails.getData())) {
            return "";
        }

        String frcName = caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName();
        return StringUtils.hasText(frcName) ? frcName : "";
    }

    /**
     * Returns the FRC email or empty string when missing.
     */
    private String getCitizenUploadCourtEmail(FinremCaseDetails caseDetails) {
        return ofNullable(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).orElse("");
    }

    /**
     * Determines whether the first hearing is in-person.
     */
    private boolean isFirstHearingInPerson(FinremCaseData caseData) {
        return ofNullable(caseData.getManageHearingsWrapper().getHearings())
            .filter(hearings -> !hearings.isEmpty())
            .map(List::getFirst)
            .map(ManageHearingsCollectionItem::getValue)
            .map(hearing -> HearingMode.IN_PERSON.equals(hearing.getHearingMode()))
            .orElse(false);
    }

    /**
     * Formats current UK time for email template variable `timeOfSubmission`.
     */
    private String getCitizenUploadTime() {
        return TIME_OF_SUBMISSION_FORMAT.format(ZonedDateTime.now(ZoneId.of("Europe/London"))).toLowerCase();
    }

    private record Recipient(String name, String email) {
    }
}
