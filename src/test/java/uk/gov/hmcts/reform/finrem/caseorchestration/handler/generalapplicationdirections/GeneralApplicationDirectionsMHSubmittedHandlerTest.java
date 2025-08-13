package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.managehearing.ManageHearingsCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsMHSubmittedHandlerTest {

    @InjectMocks
    private GeneralApplicationDirectionsMHSubmittedHandler generalApplicationDirectionsSubmittedHandler;

    @Mock
    private ManageHearingsCorresponder manageHearingsCorresponder;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(generalApplicationDirectionsSubmittedHandler, CallbackType.SUBMITTED, CaseType.CONTESTED,
            EventType.GENERAL_APPLICATION_DIRECTIONS_MH);
    }

    @Test
    void shouldSendHearingCorrespondenceOnSubmittedCallback() {
        // Arrange
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(FinremCaseData.builder().build())
            .build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            generalApplicationDirectionsSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        // Assert
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(caseDetails.getData());
        verify(manageHearingsCorresponder).sendHearingCorrespondence(callbackRequest, AUTH_TOKEN);
        verifyNoMoreInteractions(manageHearingsCorresponder);
    }
}
