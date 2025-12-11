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

@ExtendWith(MockitoExtension.class)
class ManageHearingsNotificationRequestMapperTest {

    private static final long CASE_ID = 123456789L;
    private static final String COURT_NAME = "MockedCourt";
    private static final String APPLICANT_LAST_NAME = "Applicant last name";
    private static final String RESPONDENT_LAST_NAME = "Respondent last name";
    private static final String SOLICITOR_REFERENCE = "A solicitor reference";

    private static final String APPLICANT_SOLICITOR_EMAIL = "applicantsolicitor@example.com";
    private static final String APPLICANT_SOLICITOR_NAME = "The applicant solicitor name";

    private static final String RESPONDENT_SOLICITOR_EMAIL = "respondentsolicitor@example.com";
    private static final String RESPONDENT_SOLICITOR_NAME = "The respondent solicitor name";

    private static final String INTERVENER_SOLICITOR_EMAIL = "intervenersolicitor@example.com";
    private static final String INTERVENER_SOLICITOR_NAME = "The intervener solicitor name";

    @Mock
    private HearingCorrespondenceHelper hearingCorrespondenceHelper;

    @InjectMocks
    private ManageHearingsNotificationRequestMapper mapper;

    private FinremCaseDetails caseDetails;
    private Hearing hearing;
    private ContactDetailsWrapper contactDetails;

    @BeforeEach
    void setUp() {
        caseDetails = new FinremCaseDetails();
        caseDetails.setId(CASE_ID);
        caseDetails.setCaseType(CaseType.CONTESTED);

        contactDetails = new ContactDetailsWrapper();
        contactDetails.setApplicantLname(APPLICANT_LAST_NAME);
        contactDetails.setRespondentLname(RESPONDENT_LAST_NAME);
        contactDetails.setSolicitorReference(SOLICITOR_REFERENCE);

        FinremCaseData caseData = new FinremCaseData();
        caseData.setContactDetailsWrapper(contactDetails);
        caseDetails.setData(caseData);

        hearing = new Hearing();
        hearing.setHearingType(HearingType.FDA);
        hearing.setHearingDate(LocalDate.now());
    }

    @Test
    void whenBuildingApplicantSolicitorNotificationForAddedHearing_thenMapsExpectedFields() {
        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {
            contactDetails.setApplicantSolicitorEmail(APPLICANT_SOLICITOR_EMAIL);
            contactDetails.setApplicantSolicitorName(APPLICANT_SOLICITOR_NAME);

            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.ADD_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);
            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn(COURT_NAME);

            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, hearing);

            assertCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo(APPLICANT_SOLICITOR_EMAIL);
            assertThat(result.getName()).isEqualTo(APPLICANT_SOLICITOR_NAME);
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    @Test
    void whenBuildingApplicantSolicitorNotificationForVacatedHearingWithoutRelist_thenMapsVacatedHearingFieldsOnly() {
        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {
            caseDetails.getData().getManageHearingsWrapper().setWasRelistSelected(YesOrNo.NO);

            VacateOrAdjournedHearing vacatedHearing = VacateOrAdjournedHearing.builder()
                .vacateOrAdjournReason(VacateOrAdjournReason.CASE_NOT_READY)
                .hearingType(HearingType.DIR)
                .hearingDate(LocalDate.now())
                .hearingTime("10:00 AM")
                .build();

            contactDetails.setApplicantSolicitorEmail(APPLICANT_SOLICITOR_EMAIL);
            contactDetails.setApplicantSolicitorName(APPLICANT_SOLICITOR_NAME);

            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.VACATE_HEARING);
            mocked.when(() -> CourtHelper.getFRCForHearing(vacatedHearing)).thenReturn(COURT_NAME);

            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, vacatedHearing);

            assertCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo(APPLICANT_SOLICITOR_EMAIL);
            assertThat(result.getName()).isEqualTo(APPLICANT_SOLICITOR_NAME);
            assertThat(result.getVacatedHearingType()).isEqualTo(HearingType.DIR.getId());
        }
    }

    @Test
    void whenBuildingApplicantSolicitorNotificationForVacatedAndRelistedHearing_thenMapsVacatedAndNewHearingFields() {
        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {
            caseDetails.getData().getManageHearingsWrapper().setWasRelistSelected(YesOrNo.YES);

            VacateOrAdjournedHearing vacatedHearing = VacateOrAdjournedHearing.builder()
                .vacateOrAdjournReason(VacateOrAdjournReason.CASE_NOT_READY)
                .hearingType(HearingType.DIR)
                .hearingDate(LocalDate.now())
                .hearingTime("10:00 AM")
                .build();

            contactDetails.setApplicantSolicitorEmail(APPLICANT_SOLICITOR_EMAIL);
            contactDetails.setApplicantSolicitorName(APPLICANT_SOLICITOR_NAME);

            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.VACATE_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);
            mocked.when(() -> CourtHelper.getFRCForHearing(vacatedHearing)).thenReturn(COURT_NAME);

            NotificationRequest result = mapper.buildHearingNotificationForApplicantSolicitor(caseDetails, vacatedHearing);

            assertCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo(APPLICANT_SOLICITOR_EMAIL);
            assertThat(result.getName()).isEqualTo(APPLICANT_SOLICITOR_NAME);
            assertThat(result.getVacatedHearingType()).isEqualTo(HearingType.DIR.getId());
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    @Test
    void whenBuildingRespondentSolicitorNotificationForAddedHearing_thenMapsExpectedFields() {
        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {
            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.ADD_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);

            contactDetails.setRespondentSolicitorEmail(RESPONDENT_SOLICITOR_EMAIL);
            contactDetails.setRespondentSolicitorName(RESPONDENT_SOLICITOR_NAME);

            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn(COURT_NAME);

            NotificationRequest result = mapper.buildHearingNotificationForRespondentSolicitor(caseDetails, hearing);

            assertCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo(RESPONDENT_SOLICITOR_EMAIL);
            assertThat(result.getName()).isEqualTo(RESPONDENT_SOLICITOR_NAME);
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    @Test
    void whenBuildingIntervenerSolicitorNotificationForAddedHearing_thenMapsExpectedFields() {
        try (MockedStatic<CourtHelper> mocked = mockStatic(CourtHelper.class)) {
            when(hearingCorrespondenceHelper.getManageHearingsAction(any()))
                .thenReturn(ManageHearingsAction.ADD_HEARING);
            when(hearingCorrespondenceHelper.getActiveHearingInContext(any(), any()))
                .thenReturn(hearing);

            IntervenerWrapper intervener = new IntervenerOne();
            intervener.setIntervenerSolEmail(INTERVENER_SOLICITOR_EMAIL);
            intervener.setIntervenerSolName(INTERVENER_SOLICITOR_NAME);

            mocked.when(() -> CourtHelper.getFRCForHearing(hearing)).thenReturn(COURT_NAME);

            NotificationRequest result = mapper.buildHearingNotificationForIntervenerSolicitor(caseDetails, hearing, intervener);
            
            assertCommonNotificationRequestAttributes(result);
            assertThat(result.getNotificationEmail()).isEqualTo(INTERVENER_SOLICITOR_EMAIL);
            assertThat(result.getName()).isEqualTo(INTERVENER_SOLICITOR_NAME);
            assertThat(result.getHearingType()).isEqualTo(HearingType.FDA.getId());
        }
    }

    private void assertCommonNotificationRequestAttributes(NotificationRequest result) {
        assertThat(result.getCaseReferenceNumber()).isEqualTo(String.valueOf(CASE_ID));
        assertThat(result.getSolicitorReferenceNumber()).isEqualTo(SOLICITOR_REFERENCE);
        assertThat(result.getApplicantName()).isEqualTo(APPLICANT_LAST_NAME);
        assertThat(result.getRespondentName()).isEqualTo(RESPONDENT_LAST_NAME);
        assertThat(result.getCaseType()).isEqualTo(EmailService.CONTESTED);
        assertThat(result.getSelectedCourt()).isEqualTo(COURT_NAME);
    }
}
