package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import static org.assertj.core.api.Assertions.assertThat;

class CUIDocumentsUploadedCorresponderTest {

    private final CUIDocumentsUploadedCorresponder underTest = new CUIDocumentsUploadedCorresponder();

    @Test
    void shouldBuildEventForCitizenApplicantWhenEmailPresent() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_APPLICANT
        );

        assertThat(event.getNotificationParties()).containsExactly(NotificationParty.CITIZEN_APPLICANT);
        assertThat(event.getEmailNotificationRequest().getCaseReferenceNumber()).isEqualTo("12345");
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isNull();
    }

    @Test
    void shouldBuildEventForCitizenRespondentWhenEmailPresent() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_RESPONDENT
        );

        assertThat(event.getNotificationParties()).containsExactly(NotificationParty.CITIZEN_RESPONDENT);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isNull();
    }

    private FinremCaseDetails caseDetails(String applicantEmail, String respondentEmail) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseId("12345")
            .ccdCaseType(CaseType.CONTESTED)
            .build();

        caseData.getContactDetailsWrapper().setApplicantFmName("Applicant");
        caseData.getContactDetailsWrapper().setApplicantLname("Name");
        caseData.getContactDetailsWrapper().setRespondentFmName("Respondent");
        caseData.getContactDetailsWrapper().setRespondentLname("Name");
        caseData.getContactDetailsWrapper().setApplicantEmail(applicantEmail);
        caseData.getContactDetailsWrapper().setRespondentEmail(respondentEmail);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(caseData)
            .build();
        caseDetails.setCaseType(CaseType.CONTESTED);
        return caseDetails;
    }
}
