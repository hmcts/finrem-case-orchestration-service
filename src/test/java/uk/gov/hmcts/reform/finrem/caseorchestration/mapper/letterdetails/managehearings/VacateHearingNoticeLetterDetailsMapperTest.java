package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.managehearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.HearingNoticeLetterDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class VacateHearingNoticeLetterDetailsMapperTest {

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    private VacateHearingNoticeLetterDetailsMapper vacateHearingNoticeLetterDetailsMapper;

    @BeforeEach
    void setUp() {
        vacateHearingNoticeLetterDetailsMapper =
            new VacateHearingNoticeLetterDetailsMapper(courtDetailsConfiguration, new ObjectMapper());
    }

    @ParameterizedTest
    @MethodSource("provideHearingDetails")
    void shouldBuildDocumentTemplateDetails(YesOrNo civilPartnership, String schedule1OrMatrimonial) {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .civilPartnership(civilPartnership)
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantFmName("John")
                .applicantLname("Doe")
                .respondentFmName("Jane")
                .respondentLname("Smith")
                .solicitorReference(TestConstants.TEST_SOLICITOR_REFERENCE)
                .respondentSolicitorReference(TestConstants.TEST_RESP_SOLICITOR_REFERENCE)
                .build())
            .scheduleOneWrapper(ScheduleOneWrapper
                .builder()
                .typeOfApplication(Schedule1OrMatrimonialAndCpList.forValue(schedule1OrMatrimonial))
                .build())
            .manageHearingsWrapper(
                ManageHearingsWrapper.builder()
                    .workingHearing(WorkingHearing.builder()
                        .hearingTypeDynamicList(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(HearingType.FDR.name())
                                .label(HearingType.FDR.getId())
                                .build())
                            .build())
                        .hearingDate(LocalDate.of(2025, 8, 1))
                        .hearingTime("10:00 AM")
                        .hearingCourtSelection(Court.builder()
                            .region(Region.LONDON)
                            .londonList(RegionLondonFrc.LONDON)
                            .courtListWrapper(DefaultCourtListWrapper.builder()
                                .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                .build())
                            .build())
                        .partiesOnCaseMultiSelectList(DynamicMultiSelectList.builder()
                            .value(List.of(
                                DynamicMultiSelectListElement.builder()
                                    .code("[APPSOLICITOR]")
                                    .label("Applicant Solicitor - Hamzah")
                                    .build()
                            ))
                            .build())
                        .build())
                    .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .data(caseData)
            .build();

        CourtDetailsTemplateFields courtTemplateFields = CourtDetailsTemplateFields.builder()
            .courtName("London Court")
            .phoneNumber("010000 00000")
            .email("email@test.com")
            .courtAddress("123 Court Street, London")
            .build();

        CourtDetails courtDetails = CourtDetails.builder()
            .courtName("London Court")
            .phoneNumber("010000 00000")
            .email("email@test.com")
            .courtAddress("123 Court Street, London")
            .build();

        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of("FR_s_CFCList_1", courtDetails));

        // Act
        DocumentTemplateDetails result = vacateHearingNoticeLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails);

        // Assert.  Note that Vacate Hearings reuses HearingNoticeLetterDetails.  The values for each template are almost the same.
        HearingNoticeLetterDetails vacateHearingNoticeDetails = (HearingNoticeLetterDetails) result;
        assertThat(vacateHearingNoticeDetails.getCcdCaseNumber()).isEqualTo(CASE_ID);
        assertThat(vacateHearingNoticeDetails.getApplicantName()).isEqualTo("John Doe");
        assertThat(vacateHearingNoticeDetails.getRespondentName()).isEqualTo("Jane Smith");
        assertThat(vacateHearingNoticeDetails.getHearingType()).isEqualTo("Financial Dispute Resolution (FDR)");
        assertThat(vacateHearingNoticeDetails.getHearingDate()).isEqualTo("2025-08-01");
        assertThat(vacateHearingNoticeDetails.getHearingTime()).isEqualTo("10:00 AM");
        assertThat(vacateHearingNoticeDetails.getCourtDetails()).isEqualTo(courtTemplateFields);
        assertThat(vacateHearingNoticeDetails.getHearingVenue()).isEqualTo("London Court, 123 Court Street, London");
        assertThat(vacateHearingNoticeDetails.getCivilPartnership()).isEqualTo(civilPartnership.getYesOrNo());
        assertThat(vacateHearingNoticeDetails.getTypeOfApplication()).isEqualTo(schedule1OrMatrimonial);

        // Note: Hard coded until dependent code merged.
        assertThat(vacateHearingNoticeDetails.getVacateHearingReasons()).isEqualTo("A reason to vacate");
    }

    private static Stream<Arguments> provideHearingDetails() {
        return Stream.of(
            Arguments.of(
                YesOrNo.YES,
                Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989.getValue()
            ),
            Arguments.of(
                YesOrNo.NO,
                Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.getValue()
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenWorkingHearingIsNull() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder().workingHearing(null).build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .data(caseData)
            .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> vacateHearingNoticeLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails));
        assertThat(exception.getMessage()).isEqualTo("Working hearing is null");
    }
}
