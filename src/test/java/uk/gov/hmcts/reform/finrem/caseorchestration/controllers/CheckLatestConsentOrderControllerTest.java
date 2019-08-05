package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import javax.ws.rs.core.MediaType;
import java.io.File;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;

@WebMvcTest(CheckLatestConsentOrderController.class)
public class CheckLatestConsentOrderControllerTest extends BaseControllerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    public String endpoint() {
        return "/case-orchestration/check-latest-consent-order";
    }

    @Test
    public void latestConsentOrderFieldIsNullAndThrowsError() throws Exception {
        doValidCaseDataSetUp();

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/invalid-latest-consent-order.json").toURI()));
        mvc.perform(post(endpoint())
            .content(requestContent.toString())
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.errors[0]").exists())
            .andExpect(jsonPath("$.errors[0]").isNotEmpty())
            .andExpect(jsonPath("$.errors[0]").isString());
    }

    @Test
    public void latestConsentOrderFieldIsPopulatedAndProceeds() throws Exception {
        doValidCaseDataSetUp();

        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/valid-latest-consent-order.json").toURI()));
        mvc.perform(post(endpoint())
            .content(requestContent.toString())
            .header("Authorization", AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

}