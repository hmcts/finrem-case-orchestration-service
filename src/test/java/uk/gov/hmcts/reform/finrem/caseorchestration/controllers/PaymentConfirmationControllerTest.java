package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PaymentConfirmationService;

import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(PaymentConfirmationController.class)
public class PaymentConfirmationControllerTest extends BaseControllerTest {

    private static final String PBA_CONFIRMATION_URL = "/case-orchestration/payment-confirmation";

    @MockBean
    private PaymentConfirmationService paymentConfirmationService;

    private void doConfirmationSetup(boolean isConsented, boolean isHwf) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String inputFile;
        if (isConsented) {
            inputFile = isHwf ? "/fixtures/hwf.json" : "/fixtures/pba-validate.json";
        } else {
            inputFile = isHwf ? "/fixtures/contested/hwf.json" : "/fixtures/contested/pba-validate.json";
        }

        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(inputFile).toURI()));

        if (isConsented) {
            if (isHwf) {
                when(paymentConfirmationService.consentedHwfPaymentConfirmation())
                        .thenReturn("consented_hwf_confirmation_markup");
            } else {
                when(paymentConfirmationService.consentedPbaPaymentConfirmation())
                        .thenReturn("consented_pba_confirmation_markup");
            }
        } else {
            if (isHwf) {
                when(paymentConfirmationService.contestedHwfPaymentConfirmation())
                        .thenReturn("contested_hwf_confirmation_markup");
            } else {
                when(paymentConfirmationService.contestedPbaPaymentConfirmation())
                        .thenReturn("contested_pba_confirmation_markup");
            }
        }
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }


    @Test
    public void shouldReturnConsentedHWFConfirmationMarkdown() throws Exception {
        doConfirmationSetup(true, true);
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", isEmptyOrNullString()))
                .andExpect(jsonPath("$.confirmation_body", is("consented_hwf_confirmation_markup")));
        verify(paymentConfirmationService, times(1)).consentedHwfPaymentConfirmation();
    }

    @Test
    public void shouldReturnConsentedPBAConfirmationMarkdown() throws Exception {
        doConfirmationSetup(true, false);
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", isEmptyOrNullString()))
                .andExpect(jsonPath("$.confirmation_body", is("consented_pba_confirmation_markup")));
        verify(paymentConfirmationService, times(1)).consentedPbaPaymentConfirmation();
    }


    @Test
    public void shouldReturnContestedHWFConfirmationMarkdown() throws Exception {
        doConfirmationSetup(false, true);
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", isEmptyOrNullString()))
                .andExpect(jsonPath("$.confirmation_body", is("contested_hwf_confirmation_markup")));
        verify(paymentConfirmationService, times(1)).contestedHwfPaymentConfirmation();
    }

    @Test
    public void shouldReturnContestedPBAConfirmationMarkdown() throws Exception {
        doConfirmationSetup(false, false);
        mvc.perform(post(PBA_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmation_header", isEmptyOrNullString()))
                .andExpect(jsonPath("$.confirmation_body", is("contested_pba_confirmation_markup")));
        verify(paymentConfirmationService, times(1)).contestedPbaPaymentConfirmation();
    }
}