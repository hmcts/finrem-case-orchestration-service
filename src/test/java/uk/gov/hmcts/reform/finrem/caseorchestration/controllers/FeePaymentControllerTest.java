package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.Fee;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAValidationService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeePaymentController.class)
public class FeePaymentControllerTest extends BaseControllerTest {

    private static final String PBA_NUMBER = "PBA123";
    private static final String ADD_CASE_URL = "/case-orchestration/fee-lookup";
    private static final String PBA_VALIDATE_URL = "/case-orchestration/pba-validate";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @MockBean
    private FeeService feeService;

    @MockBean
    private PBAValidationService pbaValidationService;

    @MockBean
    private PBAPaymentService pbaPaymentService;

    private JsonNode requestContent;

    private static Fee fee() {
        Fee fee = new Fee();
        fee.setFeeAmount(BigDecimal.valueOf(10d));

        return fee;
    }

    private void doFeeLookupSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));

        when(feeService.getApplicationFee()).thenReturn(fee());
    }

    private void doEmtpyCaseDataSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/empty-casedata.json").toURI()));

        when(feeService.getApplicationFee()).thenReturn(fee());
    }


    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(ADD_CASE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is("Missing case data from CCD request.")));
    }

    @Test
    public void shouldDoFeeLookup() throws Exception {
        doFeeLookupSetUp();
        mvc.perform(post(ADD_CASE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amountToPay", is("1000")))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }

    private void doValidatePBASetUp(boolean isValidPBA) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/pba-validate.json").toURI()));

        when(pbaValidationService.isValidPBA(BEARER_TOKEN, PBA_NUMBER)).thenReturn(isValidPBA);
    }

    private void doHWFSetUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/hwf.json").toURI()));
    }

    @Test
    public void shouldNotDoPBAValidationWhenPaymentIsDoneWithHWF() throws Exception {
        doHWFSetUp();
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
        verify(pbaValidationService, never()).isValidPBA(anyString(), anyString());
    }

    @Test
    public void shouldReturnErrorWhenPbaValidationFails() throws Exception {
        doValidatePBASetUp(false);
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
        verify(pbaValidationService, times(1)).isValidPBA(BEARER_TOKEN, PBA_NUMBER);
    }

    @Test
    public void shouldDoPbaValidation() throws Exception {
        doValidatePBASetUp(true);
        mvc.perform(post(PBA_VALIDATE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
        verify(pbaValidationService, times(1)).isValidPBA(BEARER_TOKEN, PBA_NUMBER);
    }
}