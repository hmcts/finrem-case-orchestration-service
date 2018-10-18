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

import java.io.File;
import java.math.BigDecimal;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(CcdCallbackController.class)
@ContextConfiguration(classes = FinremCaseProgressionApplication.class)
public class CcdCallbackControllerTest {

    private static final String ADD_CASE_URL = "/case-progression/fee-lookup";
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private FeeService service;
    private MockMvc mvc;

    private JsonNode requestContent;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();

        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource("/fixtures/add-case.json").toURI()));

        Fee fee = new Fee();
        fee.setFeeAmount(new BigDecimal(10d));
        when(service.getApplicationFee()).thenReturn(fee);
    }

    @Test
    public void shouldAddCase() throws Exception {
        mvc.perform(post(ADD_CASE_URL)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeAmountToPay", is("10")))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", hasSize(0)));
    }
}