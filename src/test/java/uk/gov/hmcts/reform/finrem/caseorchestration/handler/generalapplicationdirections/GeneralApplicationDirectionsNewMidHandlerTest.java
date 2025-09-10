package uk.gov.hmcts.reform.finrem.caseorchestration.handler.generalapplicationdirections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationDirectionsNewMidHandlerTest {

    @Mock
    private ValidateHearingService validateHearingService;

    @Mock
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    @InjectMocks
    private GeneralApplicationDirectionsNewMidHandler generalApplicationDirectionsNewMidHandler;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(generalApplicationDirectionsNewMidHandler, CallbackType.MID_EVENT, CaseType.CONTESTED,
            EventType.GENERAL_APPLICATION_DIRECTIONS_MH);
    }

    @Test
    void givenHearingRequiredWithWarnings_whenHandle_thenReturnsResponseWarnings() {
        //Arrange
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();

        when(generalApplicationDirectionsService.isHearingRequired(caseDetails)).thenReturn(true);
        when(validateHearingService.validateGeneralApplicationDirectionsMandatoryParties(finremCaseData))
            .thenReturn(List.of("Mandatory party error"));
        when(validateHearingService.validateGeneralApplicationDirectionsNoticeSelection(finremCaseData))
            .thenReturn(List.of("Notice selection error"));
        when(validateHearingService.validateGeneralApplicationDirectionsIntervenerParties(finremCaseData))
            .thenReturn(List.of("Intervener warning"));
        //Act
        var response = generalApplicationDirectionsNewMidHandler.handle(callbackRequest, "authToken");

        // Assert
        assertThat(response.getErrors()).containsExactlyInAnyOrder("Mandatory party error", "Notice selection error");
        assertThat(response.getWarnings()).containsExactly("Intervener warning");
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }

    @Test
    void givenNoHearingRequired_whenHandle_thenNoValidation() {
        //Arrange
        FinremCaseData finremCaseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(caseDetails).build();

        when(generalApplicationDirectionsService.isHearingRequired(caseDetails)).thenReturn(false);

        //Act
        var response = generalApplicationDirectionsNewMidHandler.handle(callbackRequest, "authToken");

        // Assert
        verifyNoMoreInteractions(validateHearingService);
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }
}