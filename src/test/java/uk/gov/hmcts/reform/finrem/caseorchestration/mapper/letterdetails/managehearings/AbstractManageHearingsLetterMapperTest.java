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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractManageHearingsLetterMapperTest {

    @Mock
    private CourtDetailsConfiguration courtDetailsConfiguration;

    private TestAbstractManageHearingsLetterMapper testClass;

    @BeforeEach
    void setUp() {
        testClass =
            new TestAbstractManageHearingsLetterMapper(new ObjectMapper(), courtDetailsConfiguration);
    }

    @Test
    void when_buildCourtDetailsTemplateFields_given_null_raises_exception() {
        assertThat(assertThrows(IllegalArgumentException.class, () -> testClass.buildCourtDetailsTemplateFields(null)).getMessage())
            .isEqualTo("courtSelection must be provided and not blank");
    }

    @Test
    void when_buildCourtDetailsTemplateFields_given_blank_raises_exception() {
        assertThat(assertThrows(IllegalArgumentException.class, () -> testClass.buildCourtDetailsTemplateFields(" ")).getMessage())
            .isEqualTo("courtSelection must be provided and not blank");
    }

    @ParameterizedTest
    @MethodSource("provideScheduleMatrimonialOrNothing")
    void when_getSchedule1OrMatrimonial_then_correct_values_returned(Schedule1OrMatrimonialAndCpList input, String expected) {
        // given
        FinremCaseData caseData = new FinremCaseData();
        if (input != null) {
            caseData.getScheduleOneWrapper().setTypeOfApplication(input);
        }

        // when
        String result = testClass.getSchedule1OrMatrimonial(caseData);

        // then
        assertEquals(expected, result);
    }

    @Test
    void when_centralFRCCourtAddressAndEmailAreProvided_thenFieldsAreSetCorrectly() {
        String courtSelection = "CentralFRC";
        var courtDetails = CourtDetails.builder()
            .courtName("Central Family Court")
            .courtAddress("123 Main St")
            .phoneNumber("0123456789")
            .email("central@frc.gov.uk")
            .centralFRCCourtAddress("Central FRC Address")
            .centralFRCCourtEmail("centralfrc@frc.gov.uk")
            .build();
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of(courtSelection, courtDetails));

        var fields = testClass.buildCourtDetailsTemplateFields(courtSelection);
        assertThat(fields.getCentralFRCCourtAddress()).isEqualTo("Central FRC Address");
        assertThat(fields.getCentralFRCCourtEmail()).isEqualTo("centralfrc@frc.gov.uk");
    }

    @Test
    void when_centralFRCCourtAddressAndEmailAreNotProvided_thenFieldsAreSetToNull() {
        String courtSelection = "CentralFRC";
        var courtDetails = CourtDetails.builder()
            .courtName("Central Family Court")
            .courtAddress("123 Main St")
            .phoneNumber("0123456789")
            .email("central@frc.gov.uk")
            .build();
        when(courtDetailsConfiguration.getCourts()).thenReturn(Map.of(courtSelection, courtDetails));

        var fields = testClass.buildCourtDetailsTemplateFields(courtSelection);
        assertThat(fields.getCentralFRCCourtAddress()).isNull();
        assertThat(fields.getCentralFRCCourtEmail()).isNull();
    }

    @Test
    void when_getDocumentTemplateDetailsAsMap_then_expected_structure_is_returned() {
        TestDocumentTemplateDetails templateDetails = new TestDocumentTemplateDetails("test-value");
        testClass.setDocumentTemplateDetails(templateDetails);

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .build();

        Map<String, Object> detailsMap = testClass.getDocumentTemplateDetailsAsMap(caseDetails);

        assertThat(detailsMap).containsOnlyKeys("caseDetails");
        assertThat(detailsMap.get("caseDetails")).isInstanceOf(Map.class);

        Map<?, ?> caseDetailsMap = (Map<?, ?>) detailsMap.get("caseDetails");
        assertThat(caseDetailsMap.get("id")).isEqualTo(12345L);
        assertThat(caseDetailsMap.get("case_data")).isInstanceOf(Map.class);

        Map<?, ?> caseDataMap = (Map<?, ?>) caseDetailsMap.get("case_data");
        assertThat(caseDataMap.get("value")).isEqualTo("test-value");
    }

    @Test
    void when_getWorkingHearing_and_wrapper_is_null_then_exception_is_thrown() {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(null)
            .build();

        assertThat(assertThrows(IllegalArgumentException.class, () -> testClass.getWorkingHearingForTest(caseData)).getMessage())
            .isEqualTo("Working hearing is null");
    }

    @Test
    void when_getWorkingHearing_and_workingHearing_is_present_then_returns_transformed_hearing() {
        WorkingHearing workingHearing = WorkingHearing.builder()
            .hearingDate(LocalDate.of(2026, 2, 20))
            .hearingTimeEstimate("1 hour")
            .hearingTypeDynamicList(DynamicList.builder()
                .value(DynamicListElement.builder().code(HearingType.FDA.name()).build())
                .build())
            .partiesOnCaseMultiSelectList(DynamicMultiSelectList.builder()
                .value(List.of())
                .build())
            .build();

        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .workingHearing(workingHearing)
                .build())
            .build();

        Hearing hearing = testClass.getWorkingHearingForTest(caseData);

        assertThat(hearing.getHearingDate()).isEqualTo(LocalDate.of(2026, 2, 20));
        assertThat(hearing.getHearingTimeEstimate()).isEqualTo("1 hour");
        assertThat(hearing.getHearingType()).isEqualTo(HearingType.FDA);
    }

    // Underlying Docmosis template logic expects specific strings.  This will flag any changes, which could cause document
    // generation errors.
    private static Stream<Arguments> provideScheduleMatrimonialOrNothing() {
        return Stream.of(
            Arguments.of(
                Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989,
                "Under paragraph 1 or 2 of schedule 1 children act 1989"
            ),
            Arguments.of(
                Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
                "In connection to matrimonial and civil partnership proceedings"
            ),
            Arguments.of(
                null,
                "In connection to matrimonial and civil partnership proceedings"
            )
        );
    }

    // Concrete implementation for testing purposes only
    private static class TestAbstractManageHearingsLetterMapper extends AbstractManageHearingsLetterMapper {
        private DocumentTemplateDetails documentTemplateDetails;

        protected TestAbstractManageHearingsLetterMapper(
            ObjectMapper objectMapper, CourtDetailsConfiguration courtDetailsConfiguration) {
            super(objectMapper, courtDetailsConfiguration);
        }

        public void setDocumentTemplateDetails(DocumentTemplateDetails documentTemplateDetails) {
            this.documentTemplateDetails = documentTemplateDetails;
        }

        public Hearing getWorkingHearingForTest(FinremCaseData caseData) {
            return getWorkingHearing(caseData);
        }

        public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
            return documentTemplateDetails;
        }
    }

    private static class TestDocumentTemplateDetails implements DocumentTemplateDetails {
        private final String value;

        private TestDocumentTemplateDetails(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
