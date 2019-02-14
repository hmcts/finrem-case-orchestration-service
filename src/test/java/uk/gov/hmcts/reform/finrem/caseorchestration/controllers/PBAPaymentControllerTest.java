package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.fee;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.AbstractBaseController.APPLICATION_SUBMITTED_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.AbstractBaseController.AWAITING_HWF_DECISION_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler.SERVER_ERROR_MSG;

@WebMvcTest(value = {PBAPaymentController.class, FeeLookupController.class})
public class PBAPaymentControllerTest extends BaseControllerTest {

    private static final String PBA_PAYMENT_URL = "/case-orchestration/pba-payment";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @MockBean
    private FeeService feeService;

    @MockBean
    private PBAPaymentService pbaPaymentService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode requestContent;

    private static PaymentResponse paymentResponse(boolean success) {
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .reference("RC1")
                .status(success ? "success" : "failed")
                .message(success ? null : "Access denied")
                .build();
        return paymentResponse;
    }

    private void doEmtpyCaseDataSetUp() throws IOException, URISyntaxException {
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/empty-casedata.json").toURI()));
    }


    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(SERVER_ERROR_MSG)));
    }

    private void doPaymentPBASetUp(boolean success) throws Exception {
        requestContent = objectMapper.readTree(new File(getClass().getResource("/fixtures/pba-payment.json").toURI()));

        when(feeService.getApplicationFee()).thenReturn(fee());
        when(pbaPaymentService.makePayment(anyString(), anyString(), any())).thenReturn(paymentResponse(success));
    }

    private void doHWFSetUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/hwf.json").toURI()));
        when(feeService.getApplicationFee()).thenReturn(fee());
    }

    @Test
    public void shouldNotDoPBAPaymentWhenPaymentIsDoneWithHWF() throws Exception {
        doHWFSetUp();
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is(AWAITING_HWF_DECISION_STATE)))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
                .andExpect(jsonPath("$.data.amountToPay", is("1000")))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
        verify(pbaPaymentService, never()).makePayment(anyString(), anyString(), any());
    }

    @Test
    public void shouldReturnErrorWhenPbaPaymentFails() throws Exception {
        doPaymentPBASetUp(false);
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
        verify(pbaPaymentService, times(1)).makePayment(anyString(), anyString(), any());

    }

    @Test
    public void shouldDoPbaPayment() throws Exception {
        doPaymentPBASetUp(true);
        mvc.perform(post(PBA_PAYMENT_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is(APPLICATION_SUBMITTED_STATE)))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
                .andExpect(jsonPath("$.data.amountToPay", is("1000")))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
        verify(pbaPaymentService, times(1)).makePayment(anyString(), anyString(), any());
    }
}