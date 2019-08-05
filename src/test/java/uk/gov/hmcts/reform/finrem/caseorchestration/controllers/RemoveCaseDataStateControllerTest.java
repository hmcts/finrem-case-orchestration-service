package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;

@WebMvcTest(RemoveCaseDataStateController.class)
public class RemoveCaseDataStateControllerTest extends BaseControllerTest {
    public static final String REMOVE_CASE_STATE_URI = "/case-orchestration/remove-case-data-state";

    @Test
    public void shouldReturnBadRequestWhenCaseDataIsMissingInRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(REMOVE_CASE_STATE_URI)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }


    @Test
    public void shouldRemoveStateFromCaseData() throws Exception {
        doValidCaseDataSetUp();
        mvc.perform(post(REMOVE_CASE_STATE_URI)
                .content(requestContent.toString())
                .header("Authorization", BEARER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(hasKey(STATE))));
    }
}