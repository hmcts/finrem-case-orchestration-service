package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import org.junit.jupiter.api.BeforeEach;
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
    private FinremCaseDetails caseDetails;
    private Hearing hearing;
    private ContactDetailsWrapper contactDetails;

    /**
     * Data setup here should be common to all Manage Hearing Notifications.
     * Things to note:
     * - The NotificationRequest.CaseType is compared with an EmailService CaseType.
     * - Only last names are passed as names for the applicant and respondent.
     */
    @BeforeEach
    void setUp() {
        caseDetails = new FinremCaseDetails();
        caseDetails.setId(123456789L);
        caseDetails.setCaseType(CaseType.CONTESTED);
        contactDetails = new ContactDetailsWrapper();
        contactDetails.setApplicantLname("Applicant last name");
        contactDetails.setRespondentLname("Respondent last name");
        contactDetails.setSolicitorReference("A solicitor reference");
        FinremCaseData caseData = new FinremCaseData();
        caseData.setContactDetailsWrapper(contactDetails);
        caseDetails.setData(caseData);

        hearing = new Hearing();
        hearing.setHearingType(HearingType.FDA);
    }

    /**
     * Checks the specific notification request attributes for the applicant solicitor.
     */
    @Test
    void shouldBuildNotificationRequestForApplicantSolicitor() {

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            // When
            contactDetails.setApplicantSolicitorEmail("applicantsolicitor@example.com");
            contactDetails.setApplicantSolicitorName("The applicant solicitor name");
            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, hearing);

            // AssertThat
            checkCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo("applicantsolicitor@example.com");
            assertThat(result.getName()).isEqualTo("The applicant solicitor name");
        }
    }

    /**
     * Checks the specific notification request attributes for the applicant solicitor.
     */
    @Test
    void shouldBuildNotificationRequestForRespondentSolicitor() {

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            // When
            contactDetails.setRespondentSolicitorEmail("respondentsolicitor@example.com");
            contactDetails.setRespondentSolicitorName("The respondent solicitor name");
            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForRespondentSolicitor(caseDetails, hearing);

            // AssertThat
            checkCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo("respondentsolicitor@example.com");
            assertThat(result.getName()).isEqualTo("The respondent solicitor name");
        }
    }

    private void checkCommonNotificationRequestAttributes(NotificationRequest result) {
        assertThat(result.getCaseReferenceNumber()).isEqualTo("123456789");
        assertThat(result.getSolicitorReferenceNumber()).isEqualTo("A solicitor reference");
        assertThat(result.getApplicantName()).isEqualTo("Applicant last name");
        assertThat(result.getRespondentName()).isEqualTo("Respondent last name");
        assertThat(result.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        assertThat(result.getSelectedCourt()).isEqualTo("MockedCourt");
    }
}
