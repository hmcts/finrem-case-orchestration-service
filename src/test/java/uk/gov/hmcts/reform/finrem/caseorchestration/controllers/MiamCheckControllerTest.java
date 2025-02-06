package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.GlobalExceptionHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.miam.MiamCheckService;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(MiamCheckController.class)
public class MiamCheckControllerTest extends BaseControllerTest {

    private static final String API_URL = "/case-orchestration/miam-attend-exempt-check";
    private static final String ERROR_MSG = "You cannot make this application to court unless the applicant has "
        + "either attended, or is exempt from attending a MIAM. Please refer to "
        + "https://www.familymediationcouncil.org.uk/family-mediation/assessment-meeting-miam/ "
        + "for further information on what to do next and how to arrange a MIAM.";

    @MockitoBean
    private MiamCheckService service;

    @Test
    public void badRequest() throws Exception {
        doEmptyCaseDataSetUp();
        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(startsWith(GlobalExceptionHandler.SERVER_ERROR_MSG)));
    }

    @Test
    public void miamExemptErrorMessage() throws Exception {
        doValidCaseDataSetUp();
        when(service.validateMiamFields(isA(CaseDetails.class))).thenReturn(ImmutableList.of(ERROR_MSG));

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.errors[0]",
                Matchers.is(ERROR_MSG)));
    }

    @Test
    public void miamCheckNoErrorMessage() throws Exception {
        doValidCaseDataSetUp();
        when(service.validateMiamFields(isA(CaseDetails.class))).thenReturn(ImmutableList.of());

        mvc.perform(post(API_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.errors[0]").doesNotExist());
    }
}