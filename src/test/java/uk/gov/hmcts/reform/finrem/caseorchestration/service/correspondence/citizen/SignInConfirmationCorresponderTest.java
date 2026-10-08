package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames.FR_CUI_SIGN_IN_CONFIRMATION;

@ExtendWith(MockitoExtension.class)
class SignInConfirmationCorresponderTest {

    @InjectMocks
    private SignInConfirmationCorresponder underTest;

    @Test
    void givenDivorceCaseNumberAndCourtEmail_whenBuildCorrespondenceEvent_thenBuildsExpectedEventFields() {
        FinremCaseDetails caseDetails = caseDetails("divorce-123", "frc@test.justice.gov.uk");

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(
            caseDetails,
            AUTH_TOKEN,
            NotificationParty.APPLICANT
        );

        assertThat(event.getCaseDetails()).isSameAs(caseDetails);
        assertThat(event.getAuthToken()).isEqualTo(AUTH_TOKEN);
        assertThat(event.getEmailTemplate()).isEqualTo(FR_CUI_SIGN_IN_CONFIRMATION);
        assertThat(event.getNotificationParties()).containsExactly(NotificationParty.APPLICANT);

        NotificationRequest request = event.getEmailNotificationRequest();
        assertThat(request.getCaseReferenceNumber()).isEqualTo(CASE_ID);
        assertThat(request.getCaseType()).isEqualTo("contested");
        assertThat(request.getContactCourtEmail()).isEqualTo("frc@test.justice.gov.uk");
        assertThat(request.getHasDivorceAccount()).isTrue();
    }

    @Test
    void givenNullConsentOrderFrcEmail_whenBuildCorrespondenceEvent_thenSetsEmptyCourtEmail() {
        FinremCaseDetails caseDetails = caseDetails(null, null);

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(
            caseDetails,
            AUTH_TOKEN,
            NotificationParty.RESPONDENT
        );

        NotificationRequest request = event.getEmailNotificationRequest();
        assertThat(request.getContactCourtEmail()).isEmpty();
        assertThat(request.getHasDivorceAccount()).isFalse();
        assertThat(event.getNotificationParties()).isEqualTo(List.of(NotificationParty.RESPONDENT));
    }

    private FinremCaseDetails caseDetails(String divorceCaseNumber, String consentOrderFrcEmail) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId(CASE_ID)
            .ccdCaseType(CaseType.CONTESTED)
            .divorceCaseNumber(divorceCaseNumber)
            .build();

        caseData.getConsentOrderWrapper().setConsentOrderFrcEmail(consentOrderFrcEmail);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(CASE_ID_IN_LONG)
            .data(caseData)
            .build();
        caseDetails.setCaseType(CaseType.CONTESTED);
        return caseDetails;
    }
}
