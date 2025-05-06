package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managehearings;


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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageHearingsAboutToSubmitHandlerTest {

    @InjectMocks
    private ManageHearingsAboutToSubmitHandler manageHearingsAboutToSubmitHandler;

    @Mock
    private ValidateHearingService validateHearingService;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageHearingsAboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED,
            EventType.MANAGE_HEARINGS);
    }

    @Test
    void givenValidCaseData_whenHandle_thenReturnsResponseWithErrorsAndWarnings() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .hearingToAdd(ManageHearing.builder()
                    .manageHearingType(ManageHearingType.DIR)
                    .build())
                .build())
            .build();

        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .data(finremCaseData)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(validateHearingService.validateManageHearingErrors(finremCaseData))
            .thenReturn(List.of("Error 1", "Error 2"));
        when(validateHearingService.validateManageHearingWarnings(finremCaseData, ManageHearingType.DIR))
            .thenReturn(List.of("Warning 1"));

        // Act
        var response = manageHearingsAboutToSubmitHandler.handle(callbackRequest, "authToken");

        // Assert
        assertThat(response.getErrors()).containsExactly("Error 1", "Error 2");
        assertThat(response.getWarnings()).containsExactly("Warning 1");
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }

}