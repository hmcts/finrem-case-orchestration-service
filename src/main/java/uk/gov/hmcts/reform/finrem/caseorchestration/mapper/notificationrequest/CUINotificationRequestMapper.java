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

@Service
public class CUINotificationRequestMapper {

    private static final DateTimeFormatter TIME_OF_SUBMISSION_FORMAT = DateTimeFormatter.ofPattern("h:mma 'on' dd/MM/yyyy");

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
