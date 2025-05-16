package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.MissingCourtException;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.CourtDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DefaultCourtListWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SendOrderConsentForNotApprovedOrderAboutToStartHandlerTest {

    @InjectMocks
    private SendOrderConsentForNotApprovedOrderAboutToStartHandler handler;

    @Mock
    private CourtDetailsMapper courtDetailsMapper;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.SEND_ORDER);
    }

    @Test
    void givenCase_whenMissingCourtExceptionThrown_thenReturnError() {
        // Arrange
        FinremCallbackRequest callbackRequest = callbackRequest();

        // Mock the behavior of the service to throw the specific exception
        doThrow(new MissingCourtException("whatever"))
            .when(courtDetailsMapper).getCourtDetails(any(DefaultCourtListWrapper.class));

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest,
            AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNotEmpty();
        assertTrue(response.getErrors().contains("No FR court information is present on the case. "
            + "Please add this information using Update FR Court Info."));
        verify(courtDetailsMapper).getCourtDetails(any(DefaultCourtListWrapper.class));
    }

    private FinremCallbackRequest callbackRequest() {
        return FinremCallbackRequest
            .builder()
            .caseDetails(FinremCaseDetails.builder().id(123L)
                .data(new FinremCaseData()).build())
            .caseDetailsBefore(FinremCaseDetails.builder().id(123L)
                .data(new FinremCaseData()).build())
            .build();
    }
}
