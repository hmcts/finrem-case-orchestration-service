package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CUI_SIGN_IN_CONFIRMATION;

@Service
public class SignInConfirmationCorresponder {

    public SendCorrespondenceEvent buildCorrespondenceEvent(FinremCaseDetails caseDetails,
                                                             String authToken,
                                                             NotificationParty notificationParty) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getCaseIdAsString())
            .caseType(CaseType.CONTESTED.name().toLowerCase())
            .contactCourtEmail(getCitizenUploadCourtEmail(caseDetails))
            .hasDivorceAccount(caseDetails.getData().getDivorceCaseNumber() != null)
            .build();

        return SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .authToken(authToken)
            .emailTemplate(FR_CUI_SIGN_IN_CONFIRMATION)
            .emailNotificationRequest(notificationRequest)
            .notificationParties(List.of(notificationParty))
            .build();
    }

    private String getCitizenUploadCourtEmail(FinremCaseDetails caseDetails) {
        return ofNullable(caseDetails.getData().getConsentOrderWrapper().getConsentOrderFrcEmail()).orElse("");
    }
}
