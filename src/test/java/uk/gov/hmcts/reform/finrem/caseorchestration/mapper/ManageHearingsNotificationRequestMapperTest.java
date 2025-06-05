package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mockStatic;

class ManageHearingsNotificationRequestMapperTest {

    private final ManageHearingsNotificationRequestMapper mapper = new ManageHearingsNotificationRequestMapper();

    /**
     * Straightforward Mapper test.  Things to note:
     * - The CaseType is mapped to an EmailService CaseType.
     * - Only last names are passed as names for the applicant and respondent.
     */
    @Test
    void shouldBuildNotificationRequestForApplicantSolicitor() {
        // Given
        FinremCaseDetails caseDetails = new FinremCaseDetails();
        caseDetails.setId(123456789L);
        caseDetails.setCaseType(CaseType.CONTESTED);

        ContactDetailsWrapper contactDetails = new ContactDetailsWrapper();
        contactDetails.setApplicantLname("Applicant last name");
        contactDetails.setRespondentLname("Respondent last name");
        contactDetails.setSolicitorReference("A solicitor reference");
        contactDetails.setApplicantSolicitorEmail("solicitor@example.com");
        contactDetails.setApplicantSolicitorName("A solicitor Name");
        FinremCaseData caseData = new FinremCaseData();
        caseData.setContactDetailsWrapper(contactDetails);
        caseDetails.setData(caseData);

        Hearing hearing = new Hearing();
        hearing.setHearingType(HearingType.FDA);

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            // When
            mocked.when(() -> CourtHelper.getSelectedFrc(caseDetails)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, hearing);

            // Then
            assertThat(result.getNotificationEmail()).isEqualTo("solicitor@example.com");
            assertThat(result.getCaseReferenceNumber()).isEqualTo("123456789");
            assertThat(result.getSolicitorReferenceNumber()).isEqualTo("A solicitor reference");
            assertThat(result.getApplicantName()).isEqualTo("Applicant last name");
            assertThat(result.getRespondentName()).isEqualTo("Respondent last name");
            assertThat(result.getName()).isEqualTo("A solicitor Name");
            assertThat(result.getCaseType()).isEqualTo(EmailService.CONTESTED);
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }
}
