package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase.mandatorydatavalidation.RespondentSolicitorDetailsValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendApplicationContestedMidHandlerTest {
    public static final String AUTH_TOKEN = "tokien:)";
    @InjectMocks
    private AmendApplicationContestedMidHandler handler;

    @Mock
    private InternationalPostalService postalService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ExpressCaseService expressCaseService;
    @Mock
    private RespondentSolicitorDetailsValidator respondentSolicitorDetailsValidator;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.AMEND_CONTESTED_APP_DETAILS);
    }

    @Test
    void testPostalServiceValidationCalled() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(postalService, times(1)).validate(callbackRequest.getCaseDetails().getData());
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

    /**
     * This test checks that the handler will return an error message if the applicant and respondent
     * have the same solicitor.
     */
    @Test
    void givenConsentedCase_WhenApplicantAndRespondentHaveSameSolicitor_thenHandlerWillShowErrorMessage() {
        FinremCallbackRequest finremCallbackRequest = buildCallbackRequest();

        String error = "Applicant organisation cannot be the same as respondent organisation";
        when(respondentSolicitorDetailsValidator.validate(any(FinremCaseData.class)))
            .thenReturn(List.of(error));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle = handler.handle(finremCallbackRequest, AUTH_TOKEN);

        assertThat(handle.getErrors()).contains(error);
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
