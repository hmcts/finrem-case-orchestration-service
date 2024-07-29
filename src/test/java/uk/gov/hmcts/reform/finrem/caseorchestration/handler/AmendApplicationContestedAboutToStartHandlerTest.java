package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.MiamWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamLegacyExemptionsService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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

public class AmendApplicationContestedAboutToStartHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";
    private AmendApplicationContestedAboutToStartHandler handler;

    @Before
    public void setup() {
        FinremCaseDetailsMapper finremCaseDetailsMapper = Mockito.mock(FinremCaseDetailsMapper.class);
        handler = new AmendApplicationContestedAboutToStartHandler(finremCaseDetailsMapper,
            new OnStartDefaultValueService(),
            new MiamLegacyExemptionsService());
    }

    @Test
    public void givenContestedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            is(false));
    }

    @Test
    public void givenConsentedCase_whenEventIsAmendAndCallbackIsSubmitted_thenHandlerCanNotHandle() {
        assertThat(handler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.ISSUE_APPLICATION),
            is(false));
    }

    @Test
    public void givenContestedCase_whenEventIsAmend_thenHandlerCanHandle() {
        assertThat(handler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            is(true));
    }

    @Test
    public void handle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertEquals(NO_VALUE, response.getData().getCivilPartnership().getYesOrNo());
        assertEquals(MATRIMONIAL_AND_CIVIL_PARTNERSHIP_PROCEEDINGS,
            response.getData().getScheduleOneWrapper().getTypeOfApplication());
        assertFalse(response.hasWarnings());
    }

    @Test
    public void givenCaseWithInvalidLegacyMiamExemptions_whenHandle_thenReturnsWarnings() {
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
    public void givenCaseWithValidLegacyMiamExemptions_whenHandle_thenConvertsExemptionsToV2() {
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

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.ISSUE_APPLICATION)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(new FinremCaseData()).build())
            .build();
    }
}