package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CallbackDispatchService;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(CcdCallbackController.class)
public class CcdCallbackControllerTest extends BaseControllerTest {
    private static final String COS_ABOUT_TO_START_ENDPOINT = "/case-orchestration/ccdAboutToStartEvent";
    private static final String COS_MID_EVENT_ENDPOINT = "/case-orchestration/ccdMidEvent";
    private static final String COS_ABOUT_TO_SUBMIT_ENDPOINT = "/case-orchestration/ccdAboutToSubmitEvent";
    private static final String COS_SUBMITTED_ENDPOINT = "/case-orchestration/ccdSubmittedEvent";

    @MockBean
    private CallbackDispatchService callbackDispatchService;


    @Before
    public void testSetup() throws Exception {
        setUp();
        requestContent = objectMapper.readTree(new File(getClass()
            .getResource("/fixtures/simpleContestedCallbackRequest.json").toURI()));
    }

    @Test
    public void givenEmptyCcdCallback_WhenAboutToSubmit_ThenCallDispatcher() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(COS_ABOUT_TO_START_ENDPOINT)
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCcdCallback_WhenAboutToSubmit_ThenCallDispatcher() throws Exception {


        ResultActions result = mvc.perform(post(COS_ABOUT_TO_SUBMIT_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());

        verify(callbackDispatchService).dispatchToHandlers(eq(CallbackType.ABOUT_TO_SUBMIT), any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenCcdCallback_WhenAboutToStart_ThenCallDispatcher() throws Exception {

        ResultActions result = mvc.perform(post(COS_ABOUT_TO_START_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());

        verify(callbackDispatchService).dispatchToHandlers(eq(CallbackType.ABOUT_TO_START), any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenCcdCallback_WhenMidEvent_ThenCallDispatcher() throws Exception {

        ResultActions result = mvc.perform(post(COS_MID_EVENT_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());

        verify(callbackDispatchService).dispatchToHandlers(eq(CallbackType.MID_EVENT), any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenCcdCallback_WhenSubmitted_ThenCallDispatcher() throws Exception {

        ResultActions result = mvc.perform(post(COS_SUBMITTED_ENDPOINT)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE));

        result.andExpect(status().isOk());
        result.andDo(print());

        verify(callbackDispatchService).dispatchToHandlers(eq(CallbackType.SUBMITTED), any(), eq(AUTH_TOKEN));
    }
}