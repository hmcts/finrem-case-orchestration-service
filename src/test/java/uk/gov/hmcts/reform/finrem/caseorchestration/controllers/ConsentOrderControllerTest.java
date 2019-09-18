package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import javax.ws.rs.core.MediaType;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;

@WebMvcTest(ConsentOrderController.class)
public class ConsentOrderControllerTest extends BaseControllerTest {
    private static final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9";

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
        when(idamService.isUserRoleAdmin(any())).thenReturn(false);
        mvc.perform(post("/case-orchestration/update-latest-consent-order")
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.latestConsentOrder").exists())
                .andExpect(jsonPath("$.data.applicantRepresented").value("Yes"))
                .andExpect(jsonPath("$.warnings").doesNotExist());

    }

    @Test
    public void shouldThrowHttpError400() throws Exception {
        mvc.perform(post("/case-orchestration/update-latest-consent-order")
                .content("kwuilebge")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(getClass()
                .getResource(jsonFixture()).toURI()));
    }

    private String jsonFixture() {
        return "/fixtures/latestConsentedConsentOrder/amend-consent-order-by-solicitor.json";
    }

}