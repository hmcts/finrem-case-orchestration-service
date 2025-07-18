package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.fee;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TYPE_OF_APPLICATION_DEFAULT_TO;

@WebMvcTest(FeeLookupController.class)
public class FeeLookupControllerTest extends BaseControllerTest {

    private static final String FEE_LOOKUP_URL = "/case-orchestration/fee-lookup";

    @MockitoBean
    private FeeService feeService;
    @MockitoBean
    private CaseDataService caseDataService;

    private void doFeeLookupSetUp(ApplicationType applicationType) throws IOException, URISyntaxException {
        String fileName = applicationType == CONSENTED
            ? "/fixtures/fee-lookup.json" : "/fixtures/contested/fee-lookup.json";
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(fileName).toURI()));
        String typeOfApplication  =  applicationType == CONTESTED ? TYPE_OF_APPLICATION_DEFAULT_TO : null;
        when(feeService.getApplicationFee(applicationType, typeOfApplication)).thenReturn(fee(applicationType));
    }

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmptyCaseDataSetUp();
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);

        mvc.perform(post(FEE_LOOKUP_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(startsWith(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void shouldDoConsentedFeeLookup() throws Exception {
        doFeeLookupSetUp(CONSENTED);
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(true);

        mvc.perform(post(FEE_LOOKUP_URL)
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
    }

    @Test
    public void shouldDoContestedFeeLookup() throws Exception {
        doFeeLookupSetUp(CONTESTED);
        when(caseDataService.isConsentedApplication(any(CaseDetails.class))).thenReturn(false);

        mvc.perform(post(FEE_LOOKUP_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeCode", is("FEE0640")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeAmount", is("25500")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeDescription", is("finrem")))
            .andExpect(jsonPath("$.data.orderSummary.Fees[0].value.FeeVersion", is("v1")))
            .andExpect(jsonPath("$.data.amountToPay", is("25500")))
            .andExpect(jsonPath("$.errors", is(emptyOrNullString())))
            .andExpect(jsonPath("$.warnings", is(emptyOrNullString())));
    }
}
