package uk.gov.hmcts.reform.finrem.finremcaseprogression.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.FinremCaseProgressionApplication;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.fee.Fee;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.service.FeeService;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.service.PaymentByAccountService;

import java.io.File;
import java.math.BigDecimal;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(FeePaymentController.class)
@ContextConfiguration(classes = FinremCaseProgressionApplication.class)
public class FeePaymentControllerTest {

    private static final String PBA_NUMBER = "PBA";
    private static final String ADD_CASE_URL = "/case-progression/fee-lookup";
    private static final String PBA_VALIDATE_URL = "/case-progression/pba-validate/" + PBA_NUMBER;
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private FeeService feeService;

    @MockBean
    private PaymentByAccountService paymentByAccountService;
    private MockMvc mvc;

    private JsonNode requestContent;


    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    private static Fee fee() {
        Fee fee = new Fee();
        fee.setFeeAmount(new BigDecimal(10d));

        return fee;
    }

    private void doFeeLookupSetUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/fee-lookup.json").toURI()));

        when(feeService.getApplicationFee()).thenReturn(fee());
    }

    @Test
    public void shouldDoFeeLookup() throws Exception {
        doFeeLookupSetUp();

        mvc.perform(post(ADD_CASE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeAmountToPay", is("10")))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }

    private void doValidatePbaSetUp() {
        when(paymentByAccountService.isValidPBA(BEARER_TOKEN, PBA_NUMBER)).thenReturn(true);
    }

    @Test
    public void shouldDoPbaValidation() throws Exception {
        doValidatePbaSetUp();

        mvc.perform(post(PBA_VALIDATE_URL)
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}