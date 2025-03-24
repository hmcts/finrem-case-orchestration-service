package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.AMEND_CONTESTED_PAPER_APP_DETAILS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CLOSE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@ExtendWith(MockitoExtension.class)
class AmendPaperCaseContestedMidHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private AmendPaperCaseContestedMidHandler handler;

    @Mock
    private InternationalPostalService postalService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ExpressCaseService expressCaseService;


    @ParameterizedTest
    @MethodSource
    void testCanHandle(CallbackType callbackType, CaseType caseType, EventType eventType, boolean expected) {
        assertThat(handler.canHandle(callbackType, caseType, eventType)).isEqualTo(expected);
    }

    private static Stream<Arguments> testCanHandle() {
        return Stream.of(
            Arguments.of(MID_EVENT, CONTESTED, CLOSE, false),
            Arguments.of(MID_EVENT, CONTESTED, AMEND_CONTESTED_PAPER_APP_DETAILS, true),
            Arguments.of(ABOUT_TO_SUBMIT, CONSENTED, AMEND_CONTESTED_PAPER_APP_DETAILS, false),
            Arguments.of(SUBMITTED, CONTESTED, CLOSE, false)
        );
    }

    @Test
    void testGivenExpressPilotEnabled_ThenExpressCaseServiceCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, times(1)).setExpressCaseEnrollmentStatus(caseData);
        verify(expressCaseService, times(1)).setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
    }

    @Test
    void testGivenExpressPilotDisabled_ThenExpressCaseServiceIsNotCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();

        when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);

        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        FinremCaseData caseDataBefore = callbackRequest.getCaseDetailsBefore().getData();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatus(caseData);
        verify(expressCaseService, never()).setWhichExpressCaseAmendmentLabelToShow(caseData, caseDataBefore);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService).validate(callbackRequest.getCaseDetails().getData());
    }

    private FinremCallbackRequest buildCallbackRequest() {
        return FinremCallbackRequest
            .builder()
            .eventType(EventType.AMEND_CONTESTED_APP_DETAILS)
            .caseDetails(FinremCaseDetails.builder().id(123L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(456L).caseType(CONTESTED)
                .data(FinremCaseData.builder().ccdCaseType(CONTESTED).build()).build())
            .build();
    }
}
