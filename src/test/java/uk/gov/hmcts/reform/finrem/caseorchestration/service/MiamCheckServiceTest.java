package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ATTENDED_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CLAIMING_EXEMPTION_MIAM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_ABUSE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_DOMESTIC_VIOLENCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_OTHER_GROUNDS_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_PREVIOUS_ATTENDANCE_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_CHECKLIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.MIAM_URGENCY_TEXTBOX;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6;

class MiamCheckServiceTest {

    private static final String MIAM_EXEMPT_ERROR = "You cannot make this application to court unless the applicant has "
        + "either attended, or is exempt from attending a MIAM. Please refer to "
        + "https://www.familymediationcouncil.org.uk/family-mediation/assessment-meeting-miam/ "
        + "for further information on what to do next and how to arrange a MIAM.";
    private static final String MIAM_EVIDENCE_UNAVAILABLE_ERROR = "Please explain in the textbox why you are unable to "
        + "provide the required evidence with your application.";
    private static final String MIAM_LEGACY_OPTION_ERROR = "You have selected an outdated MIAM exemption option which "
        + "needs to be unchecked before you can continue.";

    @Mock
    private CaseDetails caseDetails;

    @InjectMocks
    private MiamCheckService service;

    @BeforeEach
    public void setUpCaseData() {
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @MethodSource("provideDataForMiamFieldTest")
    void whenSelectedMiamEvidenceUnavailableAndTextboxEmptyOrLegacyOptionThenShouldReturnError(Map<String, Object> data, String expectedError) {
        List<String> errors = getMiamErrorsFromCaseData(data);
        assertThat(errors, contains(expectedError));
    }

    private List<String> getMiamErrorsFromCaseData(Map<String, Object> data) {
        when(caseDetails.getData()).thenReturn(data);
        return service.validateMiamFields(caseDetails);
    }

    private static Stream<Arguments> provideDataForMiamFieldTest() {
        return Stream.of(
            Arguments.of(ImmutableMap.of(MIAM_URGENCY_CHECKLIST,
                FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6.getValue()), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_DOMESTIC_VIOLENCE_CHECKLIST,
                FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23.getValue()), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16.getValue()), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6.getValue()), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(APPLICANT_ATTENDED_MIAM,
                NO_VALUE, CLAIMING_EXEMPTION_MIAM, NO_VALUE), MIAM_EXEMPT_ERROR),

            // Previous exemptions Do Not Use options
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_3.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_5.getValue()), MIAM_LEGACY_OPTION_ERROR),

            // Other exemption Do Not Use options
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_1.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_2.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_3.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_4.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_7.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_8.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_10.getValue()), MIAM_LEGACY_OPTION_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_11.getValue()), MIAM_LEGACY_OPTION_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForMiamFieldNoErrorTest")
    void shouldNotReturnError(Map<String, Object> data) {
        List<String> errors = getMiamErrorsFromCaseData(data);
        assertThat(errors, is(empty()));
    }

    private static Stream<Arguments> provideDataForMiamFieldNoErrorTest() {
        String text = "A";
        return Stream.of(
            Arguments.of(ImmutableMap.of(APPLICANT_ATTENDED_MIAM, NO_VALUE, CLAIMING_EXEMPTION_MIAM, YES_VALUE)),
            Arguments.of(ImmutableMap.of(MIAM_URGENCY_CHECKLIST,
                FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_6.getValue(), MIAM_URGENCY_TEXTBOX, text)),
            Arguments.of(ImmutableMap.of(MIAM_DOMESTIC_VIOLENCE_CHECKLIST,
                FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_23.getValue(), MIAM_DOMESTIC_ABUSE_TEXTBOX, text)),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST,
                FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_16.getValue(), MIAM_OTHER_GROUNDS_TEXTBOX, text)),
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST,
                FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_6.getValue(), MIAM_PREVIOUS_ATTENDANCE_TEXTBOX, text))
        );
    }
}
