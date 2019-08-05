package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;

@WebMvcTest(CurrentDateController.class)
public class CurrentDateControllerTest extends BaseControllerTest {

    private static final String API_URL = "/case-orchestration/fields/authorisation3/get-current-date";

    @Test
    public void badRequestWhenGettingCurrentDate() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void getCurrentDateForTheField() throws Exception {
        doValidCaseDataSetUp();

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.authorisation3", is(notNullValue())));

    }
}