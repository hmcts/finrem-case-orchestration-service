package uk.gov.hmcts.reform.finrem.caseorchestration.handler.casesubmission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.PaymentDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters.PBAPaymentServiceAdapter;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAPaymentClient.CLIENT_PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAPaymentClient.SERVER_PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandleAnyCaseType;

@ExtendWith(MockitoExtension.class)
class CaseSubmissionAboutToSubmitHandlerTest {

    @InjectMocks
    private CaseSubmissionAboutToSubmitHandler handler;
    @Mock
    private FinremCaseDetailsMapper caseDetailsMapper;
    @Mock
    private FeeService feeService;
    @Mock
    private PBAPaymentServiceAdapter pbaPaymentService;

    @Test
    void testCanHandle() {
        assertCanHandleAnyCaseType(handler, CallbackType.ABOUT_TO_SUBMIT, EventType.APPLICATION_PAYMENT_SUBMISSION);
    }

    @Test
    void givenHelpWithFeesCase_whenHandle_thenStateChanged() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(YesOrNo.YES, null);

        mockFeeService();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertThat(response.getErrors()).isEmpty();
        assertThat(caseData.getPaymentDetailsWrapper().getPbaPaymentReference()).isNull();
        assertThat(caseData.getPaymentDetailsWrapper().getAmountToPay()).isEqualTo(BigDecimal.valueOf(31350));
        assertThat(caseData.getPaymentDetailsWrapper().getOrderSummary()).isNotNull();
        assertThat(caseData.getState()).isEqualTo(AWAITING_HWF_DECISION.getId());
        verifyNoInteractions(pbaPaymentService);
    }

    @Test
    void givenPaymentReferenceExists_whenHandle_thenNoUpdate() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(YesOrNo.NO, "Existing ref");

        mockFeeService();
        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertThat(response.getErrors()).isEmpty();
        assertThat(caseData.getPaymentDetailsWrapper().getPbaPaymentReference()).isEqualTo("Existing ref");
        assertThat(caseData.getPaymentDetailsWrapper().getAmountToPay()).isEqualTo(BigDecimal.valueOf(31350));
        assertThat(caseData.getPaymentDetailsWrapper().getOrderSummary()).isNotNull();
        assertThat(caseData.getState()).isNull();
        verifyNoInteractions(pbaPaymentService);
    }

    @Test
    void givenPaymentRequiredAndPBAValid_whenHandle_thenPaymentMade() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(YesOrNo.NO, null);

        mockFeeService();
        mockPaymentSuccess();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertThat(response.getErrors()).isEmpty();
        assertThat(caseData.getPaymentDetailsWrapper().getPbaPaymentReference()).isEqualTo("New ref");
        assertThat(caseData.getPaymentDetailsWrapper().getAmountToPay()).isEqualTo(BigDecimal.valueOf(31350));
        assertThat(caseData.getPaymentDetailsWrapper().getOrderSummary()).isNotNull();
        assertThat(caseData.getState()).isNull();
        verify(pbaPaymentService).makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    void givenPaymentRequiredAndPBANotValid_whenHandle_thenErrorReturned() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(YesOrNo.NO, null);

        mockFeeService();
        mockPaymentFailure();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors()).containsOnly(CLIENT_PAYMENT_ERROR_MESSAGE);
        FinremCaseData caseData = response.getData();
        assertThat(caseData.getPaymentDetailsWrapper().getPbaPaymentReference()).isNull();
        assertThat(caseData.getPaymentDetailsWrapper().getAmountToPay()).isEqualTo(BigDecimal.valueOf(31350));
        assertThat(caseData.getPaymentDetailsWrapper().getOrderSummary()).isNotNull();
        assertThat(caseData.getState()).isNull();
        verify(pbaPaymentService).makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    void givenPaymentRequiredAndDuplicatePaymentMade_whenHandle_thenErrorReturned() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(YesOrNo.NO, null);

        mockFeeService();
        mockPaymentDuplicate();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertThat(response.getErrors()).containsOnly(CLIENT_PAYMENT_ERROR_MESSAGE);
        assertThat(caseData.getPaymentDetailsWrapper().getPbaPaymentReference()).isNull();
        assertThat(caseData.getPaymentDetailsWrapper().getAmountToPay()).isEqualTo(BigDecimal.valueOf(31350));
        assertThat(caseData.getPaymentDetailsWrapper().getOrderSummary()).isNotNull();
        assertThat(caseData.getState()).isNull();
        verify(pbaPaymentService).makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    @Test
    void givenPaymentRequiredAndPaymentApiServerError_whenHandle_thenErrorReturned() {
        FinremCallbackRequest callbackRequest = buildCallbackRequest(YesOrNo.NO, null);

        mockFeeService();
        mockServerError();

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = response.getData();
        assertThat(response.getErrors()).containsOnly(SERVER_PAYMENT_ERROR_MESSAGE);
        assertThat(caseData.getPaymentDetailsWrapper().getPbaPaymentReference()).isNull();
        assertThat(caseData.getPaymentDetailsWrapper().getAmountToPay()).isEqualTo(BigDecimal.valueOf(31350));
        assertThat(caseData.getPaymentDetailsWrapper().getOrderSummary()).isNotNull();
        assertThat(caseData.getState()).isNull();
        verify(pbaPaymentService).makePayment(AUTH_TOKEN, callbackRequest.getCaseDetails());
    }

    private FinremCallbackRequest buildCallbackRequest(YesOrNo helpWithFees, String pbaPaymentReference) {
        return FinremCallbackRequestFactory.from(FinremCaseDetails.builder()
            .data(FinremCaseData.builder()
                .paymentDetailsWrapper(PaymentDetailsWrapper.builder()
                    .helpWithFeesQuestion(helpWithFees)
                    .pbaPaymentReference(pbaPaymentReference)
                    .build())
                .build()
            )
        );
    }

    private void mockFeeService() {
        FeeCaseData feeCaseData = FeeCaseData.builder()
            .amountToPay("31350")
            .orderSummary(OrderSummary.builder()
                .build())
            .build();
        when(feeService.getApplicationFeeCaseData(any(FinremCaseDetails.class))).thenReturn(feeCaseData);
    }

    private void mockPaymentSuccess() {
        when(pbaPaymentService.makePayment(eq(AUTH_TOKEN), any(FinremCaseDetails.class)))
            .thenReturn(PaymentResponse.builder()
                .reference("New ref")
                .status("success")
                .build());
    }

    private void mockPaymentFailure() {
        when(pbaPaymentService.makePayment(eq(AUTH_TOKEN), any(FinremCaseDetails.class)))
            .thenReturn(PaymentResponse.builder()
                .error(HttpStatus.FORBIDDEN.toString())
                .message(CLIENT_PAYMENT_ERROR_MESSAGE)
                .build());
    }

    private void mockPaymentDuplicate() {
        when(pbaPaymentService.makePayment(eq(AUTH_TOKEN), any(FinremCaseDetails.class)))
            .thenReturn(PaymentResponse.builder()
                .error(PaymentResponse.DUPLICATE_PAYMENT_MESSAGE)
                .message(CLIENT_PAYMENT_ERROR_MESSAGE)
                .build());
    }

    private void mockServerError() {
        when(pbaPaymentService.makePayment(eq(AUTH_TOKEN), any(FinremCaseDetails.class)))
            .thenReturn(PaymentResponse.builder()
                .error(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message(SERVER_PAYMENT_ERROR_MESSAGE)
                .build());
    }
}
