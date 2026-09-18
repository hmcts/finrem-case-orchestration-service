package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.domain.EmailTemplateNames;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CitizenDocumentsUploadedCorresponderTest {

    private final CitizenDocumentsUploadedCorresponder underTest = new CitizenDocumentsUploadedCorresponder();

    @Test
    void shouldBuildEventForCitizenApplicant() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_APPLICANT
        );

        assertThat(event.getNotificationParties()).containsExactly(NotificationParty.CITIZEN_APPLICANT);
        assertThat(event.getEmailTemplate()).isEqualTo(EmailTemplateNames.FR_CUI_DOCUMENTS_UPLOADED);
        assertThat(event.getAuthToken()).isEqualTo("auth");
        assertThat(event.getEmailNotificationRequest().getCaseReferenceNumber()).isEqualTo("12345");
        assertThat(event.getEmailNotificationRequest().getCaseType()).isEqualTo("contested");
        assertThat(event.getEmailNotificationRequest().getTimeOfSubmission()).contains("on");
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isNull();
    }

    @Test
    void shouldBuildEventForCitizenRespondent() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(
            caseDetails,
            "auth",
            NotificationParty.CITIZEN_RESPONDENT
        );

        assertThat(event.getNotificationParties()).containsExactly(NotificationParty.CITIZEN_RESPONDENT);
        assertThat(event.getEmailNotificationRequest().getNotificationEmail()).isNull();
    }

    @Test
    void shouldSetCourtNameAndEmailWhenFirstHearingInPerson() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");
        caseDetails.getData().getConsentOrderWrapper().setConsentOrderFrcName("Leeds FRC");
        caseDetails.getData().getConsentOrderWrapper().setConsentOrderFrcEmail("leedsfrc@justice.gov.uk");
        caseDetails.getData().getManageHearingsWrapper().setHearings(List.of(
            ManageHearingsCollectionItem.builder()
                .value(Hearing.builder().hearingMode(HearingMode.IN_PERSON).build())
                .build()
        ));

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(caseDetails, "auth", NotificationParty.CITIZEN_APPLICANT);

        assertThat(event.getEmailNotificationRequest().getContactCourtName()).isEqualTo("Leeds FRC");
        assertThat(event.getEmailNotificationRequest().getContactCourtEmail()).isEqualTo("leedsfrc@justice.gov.uk");
    }

    @Test
    void shouldNotSetCourtNameWhenFirstHearingNotInPerson() {
        FinremCaseDetails caseDetails = caseDetails("applicant@example.com", "respondent@example.com");
        caseDetails.getData().getConsentOrderWrapper().setConsentOrderFrcName("Leeds FRC");
        caseDetails.getData().getConsentOrderWrapper().setConsentOrderFrcEmail("leedsfrc@justice.gov.uk");
        caseDetails.getData().getManageHearingsWrapper().setHearings(List.of(
            ManageHearingsCollectionItem.builder()
                .value(Hearing.builder().hearingMode(HearingMode.VIDEO_CALL).build())
                .build()
        ));

        SendCorrespondenceEvent event = underTest.buildCorrespondenceEvent(caseDetails, "auth", NotificationParty.CITIZEN_APPLICANT);

        assertThat(event.getEmailNotificationRequest().getContactCourtName()).isEmpty();
        assertThat(event.getEmailNotificationRequest().getContactCourtEmail()).isEqualTo("leedsfrc@justice.gov.uk");
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
