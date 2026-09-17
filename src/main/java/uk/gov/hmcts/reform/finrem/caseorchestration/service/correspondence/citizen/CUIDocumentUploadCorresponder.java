package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CUI_DOCUMENTS_UPLOADED;

/**
 * Builds correspondence events for CUI document upload notifications.
 *
 * <p>Recipient details and notification request fields are derived directly from
 * {@link FinremCaseDetails} using the supplied {@link NotificationParty}.</p>
 */
@Service
public class CUIDocumentUploadCorresponder {

    private static final DateTimeFormatter TIME_OF_SUBMISSION_FORMAT = DateTimeFormatter.ofPattern("h:mma 'on' dd/MM/yyyy");

    /**
     * Builds a correspondence event for a CUI document upload when a recipient email exists.
     *
     * @param caseDetails finrem case details
     * @param authToken authorization token used by notification listeners
     * @param notificationParty target citizen party to notify
     * @return populated event when recipient email exists; otherwise empty
     */
    public Optional<SendCorrespondenceEvent> buildCorrespondenceEventIfNeeded(FinremCaseDetails caseDetails,
                                                                               String authToken,
                                                                               NotificationParty notificationParty) {
        Recipient recipient = getRecipient(caseDetails, notificationParty);

        if (!StringUtils.hasText(recipient.email())) {
            return Optional.empty();
        }

        NotificationRequest notificationRequest = NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getCaseIdAsString())
            .name(recipient.name())
            .notificationEmail(recipient.email())
            .caseType(CaseType.CONTESTED.name().toLowerCase())
            .contactCourtName(getCitizenUploadCourtName(caseDetails))
            .contactCourtEmail(getCitizenUploadCourtEmail(caseDetails))
            .timeOfSubmission(getCitizenUploadTime())
            .build();

        return Optional.of(SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .authToken(authToken)
            .emailTemplate(FR_CUI_DOCUMENTS_UPLOADED)
            .emailNotificationRequest(notificationRequest)
            .notificationParties(List.of(notificationParty))
            .build());
    }

    private Recipient getRecipient(FinremCaseDetails caseDetails, NotificationParty notificationParty) {
        return switch (notificationParty) {
            case CITIZEN_APPLICANT -> new Recipient(
                caseDetails.getData().getFullApplicantName(),
                caseDetails.getData().getContactDetailsWrapper().getApplicantEmail()
            );
            case CITIZEN_RESPONDENT -> new Recipient(
                caseDetails.getData().getRespondentFullName(),
                caseDetails.getData().getContactDetailsWrapper().getRespondentEmail()
            );
            default -> throw new IllegalStateException("Unsupported notification party: " + notificationParty);
        };
    }

    private String getCitizenUploadCourtName(FinremCaseDetails caseDetails) {
        if (!isFirstHearingInPerson(caseDetails.getData())) {
            return "";
        }

        String frcName = caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcName();
        return StringUtils.hasText(frcName) ? frcName : "";
    }

    private String getCitizenUploadCourtEmail(FinremCaseDetails caseDetails) {
        return ofNullable(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).orElse("");
    }

    private boolean isFirstHearingInPerson(FinremCaseData caseData) {
        return ofNullable(caseData.getManageHearingsWrapper().getHearings())
            .filter(hearings -> !hearings.isEmpty())
            .map(List::getFirst)
            .map(ManageHearingsCollectionItem::getValue)
            .map(hearing -> HearingMode.IN_PERSON.equals(hearing.getHearingMode()))
            .orElse(false);
    }

    private String getCitizenUploadTime() {
        return TIME_OF_SUBMISSION_FORMAT.format(ZonedDateTime.now(ZoneId.of("Europe/London"))).toLowerCase();
    }

    private record Recipient(String name, String email) {
    }
}
