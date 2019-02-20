package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaymentConfirmationService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(PaymentConfirmationController.class)
public class PaymentConfirmationControllerTest extends BaseControllerTest {

    private static final String PBA_CONFIRMATION_URL = "/case-orchestration/payment-confirmation";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @MockBean
    private PaymentConfirmationService paymentConfirmationService;

    private JsonNode requestContent;


    private void doEmtpyCaseDataSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/empty-casedata.json").toURI()));
    }

    private void doConfirmationSetup(boolean isHwf) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));

        String markDownResponse = isHwf ? "hwf_confirmation_markup" : "pba_confirmation_markup";
        when(paymentConfirmationService.pbaPaymentConfirmationMarkdown()).thenReturn(markDownResponse);
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }


    @Test
    public void shouldReturnHWFConfirmationMarkdown() throws Exception {
        doConfirmationSetup(true);
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", isEmptyOrNullString()))
                .andExpect(jsonPath("$.confirmation_body", is("hwf_confirmation_markup")));
        verify(paymentConfirmationService, times(1)).pbaPaymentConfirmationMarkdown();
    }

    @Test
    public void shouldReturnPBAConfirmationMarkdown() throws Exception {
        doConfirmationSetup(false);
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", isEmptyOrNullString()))
                .andExpect(jsonPath("$.confirmation_body", is("pba_confirmation_markup")));
        verify(paymentConfirmationService, times(1)).pbaPaymentConfirmationMarkdown();
    }



}