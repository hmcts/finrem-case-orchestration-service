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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class HearingsAboutToStartHandlerTest {

    @Mock
    private PartyService partyService;

    @Mock
    private ValidateHearingService validateHearingService;

    @InjectMocks
    private ManageHearingsAboutToStartHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_HEARINGS);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder().data(
                    FinremCaseData.builder()
                        .manageHearingsWrapper(ManageHearingsWrapper.builder()
                            .manageHearingsActionSelection(ManageHearingsAction.ADD_HEARING)
                            .build())
                        .build())
                .build())
            .build();

        // Act
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, TestConstants.AUTH_TOKEN);

        // Assert
        assertNull(response.getData().getManageHearingsWrapper()
            .getManageHearingsActionSelection());
    }

    @Test
    void givenValidCaseDataWithErrors_whenHandle_thenReturnsResponseWithErrors() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .manageHearingsActionSelection(ManageHearingsAction.ADD_HEARING)
                .workingHearing(WorkingHearing.builder()
                    .hearingTypeDynamicList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(HearingType.DIR.name())
                            .label(HearingType.DIR.getId())
                            .build())
                        .build())
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

        // Act
        var response = handler.handle(callbackRequest, "authToken");

        // Assert
        assertThat(response.getErrors()).containsExactly("Error 1", "Error 2");
        assertThat(response.getData()).isEqualTo(finremCaseData);

    }
}
