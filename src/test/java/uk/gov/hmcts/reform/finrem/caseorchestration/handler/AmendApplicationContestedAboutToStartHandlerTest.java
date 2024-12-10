package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamDomesticViolence.FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGrounds.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamOtherGroundsV2.FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_5;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendance.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamPreviousAttendanceV2.FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.MiamUrgencyReason.FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

public class AmendApplicationContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendApplicationContestedAboutToStartHandler handler;

    @BeforeEach
    void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);
        handler = new AmendApplicationContestedAboutToStartHandler(finremCaseDetailsMapper,
            new OnStartDefaultValueService(),
            new MiamLegacyExemptionsService());
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS);
    }

    @Test
    void handle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(NO_VALUE, response.getData().getCivilPartnership().getYesOrNo());
        assertEquals(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
            response.getData().getScheduleOneWrapper().getTypeOfApplication());
        assertFalse(response.hasWarnings());
    }

    @Test
    void givenCaseWithInvalidLegacyMiamExemptions_whenHandle_thenReturnsWarnings() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        MiamWrapper miamWrapper = callbackRequest.getCaseDetails().getData().getMiamWrapper();
        miamWrapper.setMiamDomesticViolenceChecklist(List.of(FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1));
        miamWrapper.setMiamUrgencyReasonChecklist(List.of(FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1));
        miamWrapper.setMiamPreviousAttendanceChecklist(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2);
        miamWrapper.setMiamOtherGroundsChecklist(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertTrue(response.hasWarnings());
        assertEquals(3, response.getWarnings().size());
        assertEquals("The following MIAM exemptions are no longer valid and will be removed from the case data.",
            response.getWarnings().get(0));
        assertEquals(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_2.getText(), response.getWarnings().get(1));
        assertEquals(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_6.getText(), response.getWarnings().get(2));

        miamWrapper = response.getData().getMiamWrapper();
        assertEquals(1, miamWrapper.getMiamDomesticViolenceChecklist().size());
        assertEquals(FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1,
            miamWrapper.getMiamDomesticViolenceChecklist().get(0));
        assertEquals(1, miamWrapper.getMiamUrgencyReasonChecklist().size());
        assertEquals(FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1,
            miamWrapper.getMiamUrgencyReasonChecklist().get(0));
        assertNull(miamWrapper.getMiamPreviousAttendanceChecklist());
        assertNull(miamWrapper.getMiamOtherGroundsChecklist());
        assertNull(miamWrapper.getMiamPreviousAttendanceChecklistV2());
        assertNull(miamWrapper.getMiamOtherGroundsChecklistV2());
    }

    @Test
    void givenCaseWithValidLegacyMiamExemptions_whenHandle_thenConvertsExemptionsToV2() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        MiamWrapper miamWrapper = callbackRequest.getCaseDetails().getData().getMiamWrapper();
        miamWrapper.setMiamDomesticViolenceChecklist(List.of(FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1));
        miamWrapper.setMiamUrgencyReasonChecklist(List.of(FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1));
        miamWrapper.setMiamPreviousAttendanceChecklist(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_VALUE_1);
        miamWrapper.setMiamOtherGroundsChecklist(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_VALUE_5);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);
        assertFalse(response.hasWarnings());

        miamWrapper = response.getData().getMiamWrapper();
        assertEquals(1, miamWrapper.getMiamDomesticViolenceChecklist().size());
        assertEquals(FR_MS_MIAM_DOMESTIC_VIOLENCE_CHECKLIST_VALUE_1,
            miamWrapper.getMiamDomesticViolenceChecklist().get(0));
        assertEquals(1, miamWrapper.getMiamUrgencyReasonChecklist().size());
        assertEquals(FR_MS_MIAM_URGENCY_REASON_CHECKLIST_VALUE_1,
            miamWrapper.getMiamUrgencyReasonChecklist().get(0));
        assertNull(miamWrapper.getMiamPreviousAttendanceChecklist());
        assertNull(miamWrapper.getMiamOtherGroundsChecklist());
        assertEquals(FR_MS_MIAM_PREVIOUS_ATTENDANCE_CHECKLIST_V2_VALUE_1, miamWrapper.getMiamPreviousAttendanceChecklistV2());
        assertEquals(FR_MS_MIAM_OTHER_GROUNDS_CHECKLIST_V2_VALUE_5, miamWrapper.getMiamOtherGroundsChecklistV2());
    }

    @Test
    void testPopulateInRefugeQuestionsCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        // MockedStatic is closed after the try resources block
        try (MockedStatic<RefugeWrapperUtils> mockedStatic = mockStatic(RefugeWrapperUtils.class)) {

            handler.handle(callbackRequest, AUTH_TOKEN);
            // Check that updateRespondentInRefugeTab is called with our case details instance
            mockedStatic.verify(() -> RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails), times(1));
            mockedStatic.verify(() -> RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails), times(1));
        }
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}