package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.*;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HearingsSubmittedHandlerTest {

    @InjectMocks
    private ManageHearingsSubmittedHandler manageHearingsSubmittedHandler;

    @Mock
    private HearingService hearingService;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageHearingsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.MANAGE_HEARINGS);
    }

    @Test
    void shouldHandleSubmittedCallback() {
        // Arrange
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder().build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(finremCaseData)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingsSubmittedHandler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        // Assert
        assertThat(response.getData()).isEqualTo(finremCaseData);
        assertThat(response.getErrors()).isNullOrEmpty();
    }
}
