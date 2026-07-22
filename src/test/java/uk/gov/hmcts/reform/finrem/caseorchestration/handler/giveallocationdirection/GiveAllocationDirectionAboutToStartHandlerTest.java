package uk.gov.hmcts.reform.finrem.caseorchestration.handler.giveallocationdirection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ExpressCaseWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GiveAllocationDirectionAboutToStartHandlerTest {

    @InjectMocks
    private GiveAllocationDirectionAboutToStartHandler handler;

    @Mock
    private ExpressCaseService expressCaseService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.GIVE_ALLOCATION_DIRECTIONS_V2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldPopulateShowShouldAllocateToExpressPilot(
        boolean canSetExpressPilotStatus
    ) {
        ExpressCaseWrapper expressCaseWrapper = ExpressCaseWrapper.builder().build();

        FinremCaseData finremCaseData = FinremCaseData.builder()
            .expressCaseWrapper(expressCaseWrapper)
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);

        when(expressCaseService.canSetExpressPilotStatus(finremCaseData)).thenReturn(canSetExpressPilotStatus);

        handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(expressCaseWrapper)
            .extracting(ExpressCaseWrapper::getShowShouldAllocateToExpressPilot)
            .isEqualTo(YesOrNo.forValue(canSetExpressPilotStatus));
        verify(expressCaseService).canSetExpressPilotStatus(finremCaseData);
    }
}
