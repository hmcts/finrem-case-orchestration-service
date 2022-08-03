package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(ConsentOrderController.class)
public class ConsentOrderControllerTest extends BaseControllerTest {

    private static final String UPDATE_LATEST_CONSENT_ORDER_JSON = "/case-orchestration/update-latest-consent-order";
    private static final String AMEND_CONSENT_ORDER_BY_SOL_JSON
        = "/fixtures/latestConsentedConsentOrder/amend-consent-order-by-solicitor.json";

    @MockBean
    private ConsentOrderService consentOrderService;

    @MockBean
    private IdamService idamService;

    @Before
    public void setUp() {
        super.setUp();
        try {
            doRequestSetUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void shouldUpdateCaseDataWithLatestConsentOrder() throws Exception {
        when(consentOrderService.getLatestConsentOrderData(any(CallbackRequest.class))).thenReturn(getCaseDocument());
        when(idamService.isUserRoleAdmin(any())).thenReturn(true);
        mvc.perform(post(UPDATE_LATEST_CONSENT_ORDER_JSON)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.latestConsentOrder").exists())
            .andExpect(jsonPath("$.data.applicantRepresented").doesNotExist())
            .andExpect(jsonPath("$.warnings").doesNotExist());
    }

    @Test
    public void shouldUpdateCaseDataWithApplicantRepresented() throws Exception {
        when(consentOrderService.getLatestConsentOrderData(any(CallbackRequest.class))).thenReturn(getCaseDocument());
        when(idamService.isUserRoleAdmin(any())).thenReturn(false);
        mvc.perform(post(UPDATE_LATEST_CONSENT_ORDER_JSON)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.data.latestConsentOrder").exists())
            .andExpect(jsonPath("$.data.applicantRepresented").value("Yes"))
            .andExpect(jsonPath("$.warnings").doesNotExist());
    }

    @Test
    public void shouldThrowHttpError400() throws Exception {
        mvc.perform(post(UPDATE_LATEST_CONSENT_ORDER_JSON)
            .content("kwuilebge")
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource(AMEND_CONSENT_ORDER_BY_SOL_JSON).toURI()));
    }
}