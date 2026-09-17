package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Optional.ofNullable;

@Service
public class CUIDocumentUploadNotificationRequestMapper {

    private static final DateTimeFormatter TIME_OF_SUBMISSION_FORMAT = DateTimeFormatter.ofPattern("h:mma 'on' dd/MM/yyyy");

    public NotificationRequest build(FinremCaseDetails caseDetails, String recipientEmail, String partyName) {
        return NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getCaseIdAsString())
            .name(partyName)
            .notificationEmail(recipientEmail)
            .caseType(CaseType.CONTESTED.name().toLowerCase())
            .contactCourtName(getCitizenUploadCourtName(caseDetails))
            .contactCourtEmail(getCitizenUploadCourtEmail(caseDetails))
            .timeOfSubmission(getCitizenUploadTime())
            .build();
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
}
