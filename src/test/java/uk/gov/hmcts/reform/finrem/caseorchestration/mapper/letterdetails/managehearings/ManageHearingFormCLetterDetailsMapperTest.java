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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Region;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.RegionLondonFrc;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingMode;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingWithDynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.managehearings.FormCLetterDetails;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;

@ExtendWith(MockitoExtension.class)
class ManageHearingFormCLetterDetailsMapperTest {

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    private ManageHearingFormCLetterDetailsMapper manageHearingFormCLetterDetailsMapper;

    @BeforeEach
    void setUp() {
        manageHearingFormCLetterDetailsMapper = new ManageHearingFormCLetterDetailsMapper(courtDetailsConfiguration, new ObjectMapper());
    }

    @ParameterizedTest
    @MethodSource("provideFormCDetails")
    void shouldBuildDocumentTemplateDetails(HearingMode hearingMode, String additionalHearingInfo,
                                            String expectedAttendance, String expectedAdditionalInfo) {
        // Arrange
        FinremCaseData caseData = FinremCaseData.builder()
            .ccdCaseType(CaseType.CONTESTED)
            .contactDetailsWrapper(ContactDetailsWrapper
                .builder()
                .applicantFmName("John")
                .applicantLname("Doe")
                .respondentFmName("Jane")
                .respondentLname("Smith")
                .solicitorReference(TestConstants.TEST_SOLICITOR_REFERENCE)
                .respondentSolicitorReference(TestConstants.TEST_RESP_SOLICITOR_REFERENCE)
                .build())
            .manageHearingsWrapper(
                ManageHearingsWrapper.builder()
                    .workingHearing(HearingWithDynamicList.builder()
                        .hearingTypeDynamicList(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(HearingType.FDR.name())
                                .label(HearingType.FDR.getId())
                                .build())
                            .build())
                        .hearingDate(LocalDate.of(2025, 8, 1))
                        .hearingTime("10:00 AM")
                        .hearingTimeEstimate("2 hours")
                        .hearingMode(hearingMode)
                        .additionalHearingInformation(additionalHearingInfo)
                        .hearingCourtSelection(
                            Court.builder()
                                .region(Region.LONDON)
                                .londonList(RegionLondonFrc.LONDON)
                                .courtListWrapper(DefaultCourtListWrapper.builder()
                                    .cfcCourtList(CfcCourt.BROMLEY_COUNTY_COURT_AND_FAMILY_COURT)
                                    .build())
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
        DocumentTemplateDetails result = manageHearingFormCLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails);

        // Assert
        FormCLetterDetails formCLetterDetails = (FormCLetterDetails) result;
        assertThat(formCLetterDetails.getCaseNumber()).isEqualTo(caseData.getDivorceCaseNumber());
        assertThat(formCLetterDetails.getApplicantFmName()).isEqualTo("John");
        assertThat(formCLetterDetails.getApplicantLName()).isEqualTo("Doe");
        assertThat(formCLetterDetails.getRespondentFmName()).isEqualTo("Jane");
        assertThat(formCLetterDetails.getRespondentLName()).isEqualTo("Smith");
        assertThat(formCLetterDetails.getSolicitorReference()).isEqualTo(TestConstants.TEST_SOLICITOR_REFERENCE);
        assertThat(formCLetterDetails.getRSolicitorReference()).isEqualTo(TestConstants.TEST_RESP_SOLICITOR_REFERENCE);
        assertThat(formCLetterDetails.getHearingDate()).isEqualTo("2025-08-01");
        assertThat(formCLetterDetails.getHearingTime()).isEqualTo("10:00 AM");
        assertThat(formCLetterDetails.getTimeEstimate()).isEqualTo("2 hours");
        assertThat(formCLetterDetails.getAttendance()).isEqualTo(expectedAttendance);
        assertThat(formCLetterDetails.getAdditionalInformationAboutHearing()).isEqualTo(expectedAdditionalInfo);
        assertThat(formCLetterDetails.getCourtDetails()).isEqualTo(courtTemplateFields);
        assertThat(formCLetterDetails.getHearingDateLess35Days()).isEqualTo("2025-06-27");
        assertThat(formCLetterDetails.getHearingDateLess28Days()).isEqualTo("2025-07-04");
        assertThat(formCLetterDetails.getHearingDateLess21Days()).isEqualTo("2025-07-11");
        assertThat(formCLetterDetails.getHearingDateLess14Days()).isEqualTo("2025-07-18");
        assertThat(formCLetterDetails.getHearingDateLess7Days()).isEqualTo("2025-07-25");
        assertThat(formCLetterDetails.getFormCCreatedDate()).isEqualTo(LocalDate.now().toString());
        assertThat(formCLetterDetails.getEventDatePlus21Days()).isEqualTo(LocalDate.now().plusDays(21).toString());
    }

    private static Stream<Arguments> provideFormCDetails() {
        return Stream.of(
            Arguments.of(HearingMode.IN_PERSON, "Additional info", "In Person", "Additional info"),
            Arguments.of(null, null, "", "")
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
            () -> manageHearingFormCLetterDetailsMapper.buildDocumentTemplateDetails(caseDetails));
        assertThat(exception.getMessage()).isEqualTo("Working hearing is null");
    }
}
