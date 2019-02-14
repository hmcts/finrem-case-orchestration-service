package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.fee;

@WebMvcTest(FeeLookupController.class)
public class FeeLookupControllerTest extends BaseControllerTest {

    private static final String FEE_LOOKUP_URL = "/case-orchestration/fee-lookup";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @MockBean
    private FeeService feeService;

    private JsonNode requestContent;

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
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(FEE_LOOKUP_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void shouldDoFeeLookup() throws Exception {
        doFeeLookupSetUp();
        mvc.perform(post(FEE_LOOKUP_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("1000")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
                .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
                .andExpect(jsonPath("$.data.amountToPay", is("1000")))
                .andExpect(jsonPath("$.errors", isEmptyOrNullString()))
                .andExpect(jsonPath("$.warnings", isEmptyOrNullString()));
    }
}