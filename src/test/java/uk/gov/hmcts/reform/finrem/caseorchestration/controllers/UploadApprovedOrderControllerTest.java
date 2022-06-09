package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralApplicationDirectionsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@WebMvcTest(UploadApprovedOrderController.class)
public class UploadApprovedOrderControllerTest extends BaseControllerTest {
    private static final String ABOUT_TO_START_URL = "/case-orchestration/approved-order/about-to-start";
    private static final String ABOUT_TO_SUBMIT_URL = "/case-orchestration/approved-order/about-to-submit";
    private static final String SUBMITTED_URL = "/case-orchestration/approved-order/submitted";
    private static final String SUCCESS_KEY = "successKey";
    private static final String SUCCESS_VALUE = "successValue";

    @MockBean
    private UploadApprovedOrderService uploadApprovedOrderService;

    @MockBean
    private GeneralApplicationDirectionsService generalApplicationDirectionsService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        super.setUp();
        doValidCaseDataSetUpForAdditionalHearing();
        caseData = new HashMap<>();
        caseData.put(SUCCESS_KEY, SUCCESS_VALUE);
    }

    @Test
    public void givenValidCaseData_whenAboutToStartEndpointCalled_thenCallHandleLatestDraftDirectionOrder() throws Exception {
        when(uploadApprovedOrderService.handleLatestDraftDirectionOrder(any())).thenReturn(caseData);

        mvc.perform(post(ABOUT_TO_START_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successKey", is(SUCCESS_VALUE)));

        verify(uploadApprovedOrderService, times(1)).handleLatestDraftDirectionOrder(any());
    }

    @Test
    public void givenValidCaseData_whenAboutToSubmitEndpointCalled_thenCallHandleUploadApprovedOrderAboutToSubmit() throws Exception {
        when(uploadApprovedOrderService.handleUploadApprovedOrderAboutToSubmit(any(), eq(AUTH_TOKEN))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());

        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.successKey", is(SUCCESS_VALUE)));

        verify(uploadApprovedOrderService, times(1))
            .handleUploadApprovedOrderAboutToSubmit(any(), eq(AUTH_TOKEN));
    }

    @Test
    public void givenErrorLoggedByService_whenAboutToSubmitEndpointCalled_thenExpectErrorsInResponse() throws Exception {
        when(uploadApprovedOrderService.handleUploadApprovedOrderAboutToSubmit(any(), eq(AUTH_TOKEN))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(null).errors(List.of("Error Logged")).build()
        );

        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON_VALUE)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]", is("Error Logged")));
    }

    @Test
    public void givenValidCaseData_whenAboutToSubmitEndpointCalled_thenCallSubmitGeneralApplicationDirections() throws Exception {
        mvc.perform(post(SUBMITTED_URL)
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());

        verify(generalApplicationDirectionsService, times(1))
            .submitGeneralApplicationDirections(any(), eq(AUTH_TOKEN));
    }
}
