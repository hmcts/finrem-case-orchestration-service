package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.MiamCheckService;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.SetUpUtils.AUTH_TOKEN;

@WebMvcTest(MiamCheckController.class)
public class MiamCheckControllerTest extends BaseControllerTest {

    private static final String API_URL = "/case-orchestration/miam-attend-exempt-check";
    public static final String ERROR_MSG = "You cannot make this application unless the applicant has "
            + "either attended, or is exempt from attending a MIAM";

    @MockBean
    private MiamCheckService service;

    @Test
    public void badRequest() throws Exception {
        doEmtpyCaseDataSetUp();
        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(is(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void miamExemptErrorMessage() throws Exception {
        doValidCaseDataSetUp();
        when(service.miamExemptAttendCheck(isA(CaseDetails.class))).thenReturn(ImmutableList.of(ERROR_MSG));

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]",
                        Matchers.is(ERROR_MSG)));
    }

    @Test
    public void miamCheckNoErrorMessage() throws Exception {
        doValidCaseDataSetUp();
        when(service.miamExemptAttendCheck(isA(CaseDetails.class))).thenReturn(ImmutableList.of());

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.errors[0]").doesNotExist());
    }
}