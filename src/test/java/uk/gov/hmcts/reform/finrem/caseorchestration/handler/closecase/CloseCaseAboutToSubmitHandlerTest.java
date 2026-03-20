package uk.gov.hmcts.reform.finrem.caseorchestration.handler.closecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.casemetrics.CaseMetrics;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CaseDataMetricsWrapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class CloseCaseAboutToSubmitHandlerTest {

    @InjectMocks
    private CloseCaseAboutToSubmitHandler handler;

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.CLOSE),
            Arguments.of(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE)
        );
    }

    @ParameterizedTest
    @EnumSource(value = CaseType.class, names = {"CONSENTED", "CONTESTED"})
    void testHandleSetsCaseClosureDate(CaseType caseType) {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, caseType, FinremCaseData.builder()
            .caseDataMetricsWrapper(CaseDataMetricsWrapper.builder()
                .caseClosureDateField(LocalDate.of(2024, 6, 1))
                .build())
            .build());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        CaseMetrics caseMetrics = response.getData().getCaseDataMetricsWrapper().getCaseMetrics();
        assertThat(caseMetrics.getCaseClosureDate()).isEqualTo(LocalDate.of(2024, 6, 1));
    }
}
