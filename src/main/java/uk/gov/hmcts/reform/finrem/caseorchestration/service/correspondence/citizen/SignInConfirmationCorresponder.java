package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CUI_SIGN_IN_CONFIRMATION;

/**
 * Builds correspondence events for Citizen UI sign-in confirmation notifications.
 *
 * <p>The notification is sent after a citizen has successfully linked their
 * account to a contested Financial Remedy case.</p>
 */
@Service
public class SignInConfirmationCorresponder {

    /**
     * Builds the correspondence event used to send the Citizen UI sign-in
     * confirmation email to the specified party.
     * Only CONTESTED cases are supported
     *
     * @param caseDetails case details used to populate the notification
     * @param authToken authorisation token used when processing the correspondence event
     * @param notificationParty citizen party that should receive the notification
     * @return correspondence event configured for the sign-in confirmation email
     */
    public SendCorrespondenceEvent buildCorrespondenceEvent(FinremCaseDetails caseDetails, String authToken,
                                                            NotificationParty notificationParty) {
        NotificationRequest notificationRequest = NotificationRequest.builder()
            .caseReferenceNumber(caseDetails.getCaseIdAsString())
            .caseType(CaseType.CONTESTED.name().toLowerCase())
            .contactCourtEmail(getCitizenUploadCourtEmail(caseDetails))
            .hasDivorceAccount(StringUtils.isNotBlank(caseDetails.getData().getDivorceCaseNumber()))
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
        return ofNullable(caseDetails)
            .map(FinremCaseDetails::getData)
            .map(FinremCaseData::getConsentOrderWrapper)
            .map(ConsentOrderWrapper::getConsentOrderFrcEmail)
            .orElse("");
    }
}
