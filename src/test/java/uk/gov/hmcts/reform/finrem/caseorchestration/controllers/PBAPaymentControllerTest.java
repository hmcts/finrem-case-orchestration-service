package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;

import java.io.File;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.fee;
import static uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler.SERVER_ERROR_MSG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;

@WebMvcTest(value = {PBAPaymentController.class, FeeLookupController.class})
public class PBAPaymentControllerTest extends BaseControllerTest {

    private static final String PBA_PAYMENT_URL = "/case-orchestration/pba-payment";

    @MockBean
    private FeeService feeService;

    @MockBean
    private PBAPaymentService pbaPaymentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static PaymentResponse paymentResponse(boolean success) {
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .reference("RC1")
                .status(success ? "success" : "failed")
                .message(success ? null : "Access denied")
                .build();
        return paymentResponse;
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmptyCaseDataSetUp();
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(startsWith(SERVER_ERROR_MSG)));
    }

    private void doPBASetUp(boolean success) throws Exception {
        requestContent = objectMapper.readTree(new File(getClass().getResource("/fixtures/pba-payment.json").toURI()));

        when(feeService.getApplicationFee(CONSENTED)).thenReturn(fee(CONSENTED));
        when(pbaPaymentService.makePayment(anyString(), any())).thenReturn(paymentResponse(success));
    }

    private void doPBAPaymentReferenceAlreadyExistsSetup() throws Exception {
        String pbaPaymentAlreadyExists = "/fixtures/pba-payment-already-exists.json";
        requestContent = objectMapper.readTree(new File(getClass().getResource(pbaPaymentAlreadyExists).toURI()));

        when(feeService.getApplicationFee(CONSENTED)).thenReturn(fee(CONSENTED));
        when(pbaPaymentService.makePayment(anyString(), any())).thenReturn(paymentResponse(true));
    }

    private void doHWFSetUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/hwf.json").toURI()));
        when(feeService.getApplicationFee(CONSENTED)).thenReturn(fee(CONSENTED));
    }

    @Test
    public void shouldNotDoPBAPaymentWhenPaymentIsDoneWithHWF() throws Exception {
        doHWFSetUp();
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is(AWAITING_HWF_DECISION.toString())))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
                .andExpect(jsonPath("$.data.amountToPay", is("1000")))
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, never()).makePayment(anyString(), any());
    }

    @Test
    public void shouldReturnErrorWhenPbaPaymentFails() throws Exception {
        doPBASetUp(false);
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, times(1)).makePayment(anyString(), any());

    }

    @Test
    public void shouldDoPbaPayment() throws Exception {
        doPBASetUp(true);
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
                .andExpect(jsonPath("$.data.amountToPay", is("1000")))
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, times(1)).makePayment(anyString(), any());
    }

    @Test
    public void shouldNotDoPbaPaymentWhenPBAPaymentAlreadyExists() throws Exception {
        doPBAPaymentReferenceAlreadyExistsSetup();
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
                .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
        verify(pbaPaymentService, never()).makePayment(anyString(), any());
    }
}