package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.ThrowingRunnable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.getThrowingRunnableCaptor;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsSubmittedHandlerTest {

    @InjectMocks
    private GeneralApplicationDirectionsSubmittedHandler generalApplicationDirectionsSubmittedHandler;

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @Mock
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @Mock
    private RetryExecutor retryExecutor;

    @Test
    void testCanHandle() {
        assertCanHandle(generalApplicationDirectionsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.GENERAL_APPLICATION_DIRECTIONS_MH);
    }

    /**
     * Creates a FinremCallbackRequest with a FinremCaseDetails containing empty FinremCaseData and a fixed case ID.
     *
     * @return a FinremCallbackRequest for use in tests
     */
    private FinremCallbackRequest createCallbackRequest() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(FinremCaseData.builder().build())
            .build();
        return FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    /*
     * Verifies that when a hearing is required, the handler calls the manageHearingsCorresponder to send hearing correspondence.
     */
    @Test
    void shouldSendHearingCorrespondenceOnSubmittedCallback() {
        // Arrange
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG,
            FinremCaseData.builder().build());

        when(generalApplicationDirectionsService.isHearingRequired(callbackRequest.getCaseDetails()))
            .thenReturn(true);

        // Act
        var response = generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        ArgumentCaptor<ThrowingRunnable> sendHearingRunnableCaptor = getThrowingRunnableCaptor();
        assertAll(
            () -> assertThat(response.getErrors()).isNullOrEmpty(),
            () -> verify(retryExecutor)
                .runWithRetrySuppressException(
                    sendHearingRunnableCaptor.capture(),
                    eq("Send Hearing Correspondence (FR_GeneralApplicationDirectionsMH)"),
                    eq(CASE_ID)
                ),
            () -> {
                sendHearingRunnableCaptor.getAllValues().forEach(TestSetUpUtils::runSafely);
                verify(manageHearingsCorresponder).sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
            },
            () -> verifyNoMoreInteractions(manageHearingsCorresponder)
        );
    }

    /*
     * Verifies that when a hearing is not required, the handler does not call the manageHearingsCorresponder.
     */
    @Test
    void shouldNotSendHearingCorrespondenceWhenHearingNotRequired() {
        // Arrange
        FinremCallbackRequest callbackRequest = createCallbackRequest();
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();

        when(generalApplicationDirectionsService.isHearingRequired(caseDetails)).thenReturn(false);

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        verifyNoMoreInteractions(manageHearingsCorresponder);
    }
}
