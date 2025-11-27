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
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.CourtDetailsConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CfcCourt;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Court;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournReason;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingVacatedHearing;
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
import java.util.UUID;
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
    @MethodSource("provideHearingDetailsAffectingLogic")
    void shouldBuildDocumentTemplateDetails(YesOrNo civilPartnership, String schedule1OrMatrimonial,
                                            VacateOrAdjournReason vacateOrAdjournReason) {
        // Arrange
        FinremCaseDetails caseDetails = buildCaseDetails(civilPartnership, schedule1OrMatrimonial, vacateOrAdjournReason);

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

        if (VacateOrAdjournReason.OTHER.equals(vacateOrAdjournReason)) {
            assertThat(vacateHearingNoticeDetails.getVacateHearingReasons()).isEqualTo("illness");
        } else {
            assertThat(vacateHearingNoticeDetails.getVacateHearingReasons()).isEqualTo(vacateOrAdjournReason.getDisplayValue());
        }
    }

    private static Stream<Arguments> provideHearingDetailsAffectingLogic() {
        return Stream.of(
            Arguments.of(
                YesOrNo.YES,
                Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989.getValue(),
                VacateOrAdjournReason.OTHER

            ),
            Arguments.of(
                YesOrNo.NO,
                Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS.getValue(),
                VacateOrAdjournReason.ADJOURNED
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenWorkingVacatedHearingIsNull() {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder().workingVacatedHearing(null).build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .data(caseData)
            .build();

        // Act & Assert, static method getWorkingVacatedHearingId on ManageHearingsWrapper raises the exception.
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> vacateHearingNoticeLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails));
        assertThat(exception.getMessage()).isEqualTo("Invalid or missing working vacated hearing UUID");
    }

    /*
     * case includes this minimum detail for the mapper:
     * - contact details
     * - civil partnership details
     * - scheduleOneWrapper
     * - manageHearingsWrapper with a:
     *   - working vacated hearing
     *   - vacated/adjourned hearing
     * No working hearing required.
     */
    private FinremCaseDetails buildCaseDetails(YesOrNo civilPartnership, String schedule1OrMatrimonial,
                                               VacateOrAdjournReason vacateOrAdjournReason) {
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .civilPartnership(civilPartnership)
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantFmName("John")
                .applicantLname("Doe")
                .respondentFmName("Jane")
                .respondentLname("Smith")
                .build())
            .scheduleOneWrapper(ScheduleOneWrapper
                .builder()
                .typeOfApplication(Schedule1OrMatrimonialAndCpList.forValue(schedule1OrMatrimonial))
                .build())
            .manageHearingsWrapper(
                ManageHearingsWrapper.builder()
                    .workingVacatedHearing(WorkingVacatedHearing.builder()
                        .chooseHearings(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code("c0a78b4c-5d85-4e50-9d62-219b1b8eb9bb")
                                .build())
                            .build())
                        .build())
                    .vacatedOrAdjournedHearings(List.of(
                        VacatedOrAdjournedHearingsCollectionItem.builder()
                            .id(UUID.fromString("c0a78b4c-5d85-4e50-9d62-219b1b8eb9bb"))
                            .value(VacateOrAdjournedHearing.builder()
                                .hearingType(HearingType.FDR)
                                .hearingDate(LocalDate.of(2025, 8, 1))
                                .hearingTime("10:00 AM")
                                .vacateOrAdjournReason(vacateOrAdjournReason)
                                .specifyOtherReason("illness")
                                .hearingCourtSelection(Court.builder()
                                    .region(Region.LONDON)
                                    .londonList(RegionLondonFrc.LONDON)
                                    .courtListWrapper(DefaultCourtListWrapper.builder()
                                        .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                        .build())
                                    .build())
                                .build())
                            .build()))
                    .build())
            .build();

        return FinremCaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .data(caseData)
            .build();
    }
}
