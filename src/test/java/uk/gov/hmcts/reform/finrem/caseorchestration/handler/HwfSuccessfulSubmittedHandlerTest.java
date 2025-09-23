package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hwf.HwfCorrespondenceService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class HwfSuccessfulSubmittedHandlerTest {

    @Mock
    private HwfCorrespondenceService hwfNotificationsService;

    @Mock
    private FinremCaseDetailsMapper caseDetailsMapper;

    @InjectMocks
    private HwfSuccessfulSubmittedHandler handler;

    @Test
    void canHandle() {
        assertCanHandle(handler,
            Arguments.of(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.FR_HWF_DECISION_MADE),
            Arguments.of(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.FR_HWF_DECISION_MADE),
            Arguments.of(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.FR_HWF_DECISION_MADE_FROM_AWAITING_PAYMENT),
            Arguments.of(CallbackType.SUBMITTED, CaseType.CONSENTED, EventType.FR_HWF_DECISION_MADE_FROM_AWAITING_PAYMENT)
        );
    }

    @Test
    void givenContestedCase_whenHandle_thenCallsHwfCorrespondenceService() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .build())
                .build())
            .build();

        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(hwfNotificationsService).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenConsentedCase_whenHandle_thenCallsHwfCorrespondenceService() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .data(FinremCaseData.builder()
                    .build())
                .build())
            .build();

        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(hwfNotificationsService).sendCorrespondence(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    void givenCase_whenHandle_thenReturnsResponseWithCaseData() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .data(caseData)
                .build())
            .build();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData()).isEqualTo(caseData);
    }
}