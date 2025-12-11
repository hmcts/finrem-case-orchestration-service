package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.notificationrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.CourtHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.managehearings.HearingCorrespondenceHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.notification.NotificationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

//TODO: Tidy up AL
@ExtendWith(MockitoExtension.class)
class ManageHearingsNotificationRequestMapperTest {

    @Mock
    HearingCorrespondenceHelper hearingCorrespondenceHelper;
    @InjectMocks
    ManageHearingsNotificationRequestMapper mapper;

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
        hearing.setHearingDate(LocalDate.now());
    }

    /**
     * Checks the specific notification request attributes for the applicant solicitor.
     */
    @Test
    void shouldBuildNotificationRequestForApplicantSolicitorAddAHearing() {

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            // When
            contactDetails.setApplicantSolicitorEmail("applicantsolicitor@example.com");
            contactDetails.setApplicantSolicitorName("The applicant solicitor name");
            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.ADD_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);
            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, hearing);

            // AssertThat
            checkCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo("applicantsolicitor@example.com");
            assertThat(result.getName()).isEqualTo("The applicant solicitor name");
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    @Test
    void shouldBuildNotificationRequestForVacatedHearingApplicantSolicitor() {

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            caseDetails.getData().getManageHearingsWrapper().setWasRelistSelected(YesOrNo.NO);

            final VacateOrAdjournedHearing vacatedHearing = VacateOrAdjournedHearing
                .builder()
                .vacateOrAdjournReason(VacateOrAdjournReason.CASE_NOT_READY)
                .hearingType(HearingType.DIR)
                .hearingDate(LocalDate.now())
                .hearingTime("10:00 AM")
                .build();

            // When
            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.VACATE_HEARING);
            contactDetails.setApplicantSolicitorEmail("applicantsolicitor@example.com");
            contactDetails.setApplicantSolicitorName("The applicant solicitor name");
            mocked.when(() -> CourtHelper.getFRCForHearing(vacatedHearing)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, vacatedHearing);

            // AssertThat
            checkCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo("applicantsolicitor@example.com");
            assertThat(result.getName()).isEqualTo("The applicant solicitor name");
            assertThat(result.getVacatedHearingType()).isEqualTo(HearingType.DIR.getId());
        }
    }

    /**
     * Checks the specific notification request attributes for the applicant solicitor.
     */
    @Test
    void shouldBuildNotificationRequestForRespondentSolicitor() {

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            // When

            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.ADD_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);
            contactDetails.setRespondentSolicitorEmail("respondentsolicitor@example.com");
            contactDetails.setRespondentSolicitorName("The respondent solicitor name");
            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForRespondentSolicitor(caseDetails, hearing);

            // AssertThat
            checkCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo("respondentsolicitor@example.com");
            assertThat(result.getName()).isEqualTo("The respondent solicitor name");
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    /**
     * Checks the specific notification request attributes for the Intervener solicitor.
     * Creates an IntervenerOne object.  IntervenerTwo to Four use the same logic.
     */
    @Test
    void shouldBuildNotificationRequestForIntervenerSolicitor() {

        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {

            // When
            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.ADD_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);
            IntervenerWrapper intervener = new IntervenerOne();
            intervener.setIntervenerSolEmail("intervenersolicitor@example.com");
            intervener.setIntervenerSolName("The intervener solicitor name");
            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn("MockedCourt");
            NotificationRequest result = mapper.buildHearingNotificationForIntervenerSolicitor(caseDetails, hearing, intervener);

            // AssertThat
            checkCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo("intervenersolicitor@example.com");
            assertThat(result.getName()).isEqualTo("The intervener solicitor name");
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    private void checkCommonNotificationRequestAttributes(NotificationRequest result) {
        assertThat(result.getCaseReferenceNumber()).isEqualTo("123456789");
        assertThat(result.getSolicitorReferenceNumber()).isEqualTo("A solicitor reference");
        assertThat(result.getApplicantName()).isEqualTo("Applicant last name");
        assertThat(result.getRespondentName()).isEqualTo("Respondent last name");
        assertThat(result.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(result.getSelectedCourt()).isEqualTo("MockedCourt");
    }
}
