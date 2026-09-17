package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CUIDocumentUploadCorresponderTest {

    private final CUIDocumentUploadCorresponder underTest = new CUIDocumentUploadCorresponder();

    @Test
    void shouldBuildEventForCitizenApplicantWhenEmailPresent() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");

        Optional<SendCorrespondenceEvent> event = underTest.buildCorrespondenceEventIfNeeded(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_APPLICANT
        );

        assertThat(event).isPresent();
        assertThat(event.get().getNotificationParties()).containsExactly(NotificationParty.CITIZEN_APPLICANT);
        assertThat(event.get().getEmailNotificationRequest().getCaseReferenceNumber()).isEqualTo("12345");
        assertThat(event.get().getEmailNotificationRequest().getNotificationEmail()).isNull();
    }

    @Test
    void shouldBuildEventForCitizenRespondentWhenEmailPresent() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");

        Optional<SendCorrespondenceEvent> event = underTest.buildCorrespondenceEventIfNeeded(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_RESPONDENT
        );

        assertThat(event).isPresent();
        assertThat(event.get().getNotificationParties()).containsExactly(NotificationParty.CITIZEN_RESPONDENT);
        assertThat(event.get().getEmailNotificationRequest().getNotificationEmail()).isNull();
    }

    @Test
    void shouldReturnEmptyWhenCitizenApplicantEmailMissing() {
        FinremCaseDetails caseDetails = caseDetails("", "respondent@example.com");

        Optional<SendCorrespondenceEvent> event = underTest.buildCorrespondenceEventIfNeeded(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_APPLICANT
        );

        assertThat(event).isEmpty();
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
