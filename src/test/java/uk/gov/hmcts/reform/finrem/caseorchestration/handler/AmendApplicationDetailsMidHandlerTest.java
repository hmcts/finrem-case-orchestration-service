package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationDetailsMidHandlerTest {
    @InjectMocks
    private AmendApplicationDetailsMidHandler underTest;

    @Mock
    private InternationalPostalService internationalPostalService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(underTest,
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS),
            Arguments.of(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONTESTED_PAPER_APP_DETAILS));
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"AMEND_CONTESTED_APP_DETAILS", "AMEND_CONTESTED_PAPER_APP_DETAILS"})
    void testPostalServiceValidationCalled(EventType eventType) {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(eventType);
        underTest.handle(callbackRequest, AUTH_TOKEN);
        verify(internationalPostalService).validate(callbackRequest.getCaseDetails().getData());
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"AMEND_CONTESTED_APP_DETAILS", "AMEND_CONTESTED_PAPER_APP_DETAILS"})
    void testGivenExpressPilotEnabled_ThenExpressCaseServiceCalled(EventType eventType) {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(eventType);

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
        verify(expressCaseService).setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
    }

    @ParameterizedTest
    @EnumSource(value = EventType.class, names = {"AMEND_CONTESTED_APP_DETAILS", "AMEND_CONTESTED_PAPER_APP_DETAILS"})
    void testGivenExpressPilotDisabled_ThenExpressCaseServiceIsNotCalled(EventType eventType) {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(eventType);

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        underTest.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatus(caseData);
        verify(expressCaseService, never()).setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
    }

    private FinremCallbackRequest buildCallbackRequest(EventType eventType) {
        return FinremCallbackRequest
            .builder()
            .eventType(eventType)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(456L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .build();
    }
}
