package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(GeneralApplicationController.class)
public class GeneralApplicationControllerTest extends BaseControllerTest {

    private static final String SUBMIT_GENERAL_APPLICATION_URL = "/case-orchestration/submit-general-application";
    private static final String START_GENERAL_APPLICATION_URL = "/case-orchestration/start-general-application";

    @MockBean
    private GeneralApplicationService generalApplicationService;

    @Test
    public void submitGeneralApplication400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(SUBMIT_GENERAL_APPLICATION_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void submitGeneralApplication500Error() throws Exception {
        doValidCaseDataSetUp();
        doThrow(feignError()).when(generalApplicationService).updateCaseDataSubmit(isA(Map.class), isA(CaseDetails.class));

        mvc.perform(post(SUBMIT_GENERAL_APPLICATION_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void submitGeneralApplicationSuccess() throws Exception {
        mvc.perform(post(SUBMIT_GENERAL_APPLICATION_URL)
            .content(resourceContentAsString("/fixtures/general-application.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalApplicationService, times(1)).updateCaseDataSubmit(any(), any());
    }

    @Test
    public void startGeneralApplication400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(START_GENERAL_APPLICATION_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void startGeneralApplication500Error() throws Exception {
        doValidCaseDataSetUp();
        doThrow(feignError()).when(generalApplicationService).updateCaseDataStart(isA(Map.class), eq(AUTH_TOKEN));

        mvc.perform(post(START_GENERAL_APPLICATION_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void startGeneralApplicationSuccess() throws Exception {
        mvc.perform(post(START_GENERAL_APPLICATION_URL)
            .content(resourceContentAsString("/fixtures/general-application.json"))
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalApplicationService, times(1)).updateCaseDataStart(isA(Map.class), eq(AUTH_TOKEN));
    }
}
