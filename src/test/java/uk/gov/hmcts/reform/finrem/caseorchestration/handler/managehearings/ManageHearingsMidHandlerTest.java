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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.HearingType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsAction;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ManageHearingsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageHearingsMidHandlerTest {

    @Mock
    private ValidateHearingService validateHearingService;

    @InjectMocks
    private ManageHearingsMidHandler manageHearingsMidHandler;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageHearingsMidHandler, CallbackType.MID_EVENT, CaseType.CONTESTED,
            EventType.MANAGE_HEARINGS);
    }

    @Test
    void givenValidCaseDataWithWarnings_whenHandle_thenReturnsResponseWarnings() {
        //Arrange
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

        when(validateHearingService.validateManageHearingWarnings(finremCaseData, HearingType.DIR))
            .thenReturn(List.of("Warning 1"));

        // Act
        var response = manageHearingsMidHandler.handle(callbackRequest, "authToken");

        // Assert
        assertThat(response.getWarnings()).containsExactly("Warning 1");
        assertThat(response.getData()).isEqualTo(finremCaseData);
    }
}
