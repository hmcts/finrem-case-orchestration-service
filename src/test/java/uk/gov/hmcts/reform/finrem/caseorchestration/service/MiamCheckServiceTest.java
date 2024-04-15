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

public class MiamCheckServiceTest {

    private static final String MIAM_EXEMPT_ERROR = "You cannot make this application to court unless the applicant has "
        + "either attended, or is exempt from attending a MIAM. Please refer to "
        + "https://www.familymediationcouncil.org.uk/family-mediation/assessment-meeting-miam/ "
        + "for further information on what to do next and how to arrange a MIAM.";
    private static final String MIAM_EVIDENCE_UNAVAILABLE_ERROR = "Please explain in the textbox why you are unable to "
        + "provide the required evidence with your application.";
    private static final String URGENCY_CHECKLIST_VALUE = "FR_ms_MIAMUrgencyReasonChecklist_Value_6";
    private static final String DOMESTIC_VIOLENCE_CHECKLIST_VALUE = "FR_ms_MIAMDomesticViolenceChecklist_Value_23";
    private static final String OTHER_GROUNDS_CHECKLIST_VALUE = "FR_ms_MIAMOtherGroundsChecklist_Value_7";
    private static final String PREVIOUS_ATTENDANCE_CHECKLIST_VALUE = "FR_ms_MIAMPreviousAttendanceChecklist_Value_3";

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
    public void whenSelectedMiamEvidenceUnavailableAndTextboxEmptyThenShouldReturnError(Map<String, Object> data, String expectedError) {
        List<String> errors = getMiamErrorsFromCaseData(data);
        assertThat(errors, contains(expectedError));
    }

    private List<String> getMiamErrorsFromCaseData(Map<String, Object> data) {
        when(caseDetails.getData()).thenReturn(data);
        List<String> errors = service.validateMiamFields(caseDetails);
        return errors;
    }

    private static Stream<Arguments> provideDataForMiamFieldTest() {
        return Stream.of(
            Arguments.of(ImmutableMap.of(MIAM_URGENCY_CHECKLIST, URGENCY_CHECKLIST_VALUE), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_DOMESTIC_VIOLENCE_CHECKLIST, DOMESTIC_VIOLENCE_CHECKLIST_VALUE), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST, OTHER_GROUNDS_CHECKLIST_VALUE), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, PREVIOUS_ATTENDANCE_CHECKLIST_VALUE), MIAM_EVIDENCE_UNAVAILABLE_ERROR),
            Arguments.of(ImmutableMap.of(APPLICANT_ATTENDED_MIAM, NO_VALUE, CLAIMING_EXEMPTION_MIAM, NO_VALUE), MIAM_EXEMPT_ERROR)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDataForMiamFieldNoErrorTest")
    public void shouldNotReturnError(Map<String, Object> data) {
        List<String> errors = getMiamErrorsFromCaseData(data);
        assertThat(errors, is(empty()));
    }

    private static Stream<Arguments> provideDataForMiamFieldNoErrorTest() {
        String text = "A";
        return Stream.of(
            Arguments.of(ImmutableMap.of(APPLICANT_ATTENDED_MIAM, NO_VALUE, CLAIMING_EXEMPTION_MIAM, YES_VALUE)),
            Arguments.of(ImmutableMap.of(MIAM_URGENCY_CHECKLIST, URGENCY_CHECKLIST_VALUE, MIAM_URGENCY_TEXTBOX, text)),
            Arguments.of(ImmutableMap.of(MIAM_DOMESTIC_VIOLENCE_CHECKLIST, DOMESTIC_VIOLENCE_CHECKLIST_VALUE, MIAM_DOMESTIC_ABUSE_TEXTBOX, text)),
            Arguments.of(ImmutableMap.of(MIAM_OTHER_GROUNDS_CHECKLIST, OTHER_GROUNDS_CHECKLIST_VALUE, MIAM_OTHER_GROUNDS_TEXTBOX, text)),
            Arguments.of(ImmutableMap.of(MIAM_PREVIOUS_ATTENDANCE_CHECKLIST, PREVIOUS_ATTENDANCE_CHECKLIST_VALUE,
                MIAM_PREVIOUS_ATTENDANCE_TEXTBOX, text))
        );
    }
}