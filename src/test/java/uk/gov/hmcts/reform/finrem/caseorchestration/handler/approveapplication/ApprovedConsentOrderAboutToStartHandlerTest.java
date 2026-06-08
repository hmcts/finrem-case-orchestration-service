package uk.gov.hmcts.reform.finrem.caseorchestration.handler.approveapplication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ApprovedConsentOrderAboutToStartHandlerTest {

    @InjectMocks
    private ApprovedConsentOrderAboutToStartHandler handler;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.APPROVE_ORDER);
    }

    @Test
    void shouldApplyDefaultWhenHandled() {

        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(finremCaseData);

        var response = handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> assertThat(response.getData()).isEqualTo(finremCaseData),
            () -> verify(onStartDefaultValueService).defaultConsentedOrderJudgeName(finremCaseData, AUTH_TOKEN),
            () -> verify(onStartDefaultValueService).defaultConsentedOrderDate(finremCaseData)
        );
    }
}
