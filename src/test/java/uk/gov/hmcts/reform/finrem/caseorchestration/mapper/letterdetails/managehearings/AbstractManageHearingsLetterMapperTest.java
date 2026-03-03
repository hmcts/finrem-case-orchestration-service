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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.letterdetails.DocumentTemplateDetails;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertNull(fields.getCentralFRCCourtAddress());
        assertNull(fields.getCentralFRCCourtEmail());
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
        protected TestAbstractManageHearingsLetterMapper(
            ObjectMapper objectMapper, CourtDetailsConfiguration courtDetailsConfiguration) {
            super(objectMapper, courtDetailsConfiguration);
        }

        public DocumentTemplateDetails buildDocumentTemplateDetails(FinremCaseDetails caseDetails) {
            return null; // not used in these tests
        }
    }
}
