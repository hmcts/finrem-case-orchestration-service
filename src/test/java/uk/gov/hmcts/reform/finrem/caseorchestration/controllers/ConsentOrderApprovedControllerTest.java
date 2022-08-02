package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.ConsentOrderApprovedHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.serialisation.FinremCallbackRequestDeserializer;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.feignError;

@WebMvcTest(ConsentOrderApprovedController.class)
public class ConsentOrderApprovedControllerTest extends BaseControllerTest {

    @MockBean private ConsentOrderApprovedHandler consentOrderApprovedHandler;
    @MockBean private FinremCallbackRequestDeserializer finremCallbackRequestDeserializer;

    public String consentOrderApprovedEndpoint() {
        return "/case-orchestration/documents/consent-order-approved";
    }

    public String contestedConsentOrderApprovedEndpoint() {
        return "/case-orchestration/consent-in-contested/consent-order-approved";
    }

    public String contestedConsentSendOrderEndpoint() {
        return "/case-orchestration/consent-in-contested/send-order";
    }

    @Test
    public void consentOrderApproved400Error() throws Exception {
        doEmptyCaseDataSetUp();

        mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void consentOrderApproved500Error() throws Exception {
        doValidCaseDataSetUp();

        when(finremCallbackRequestDeserializer.deserialize(any())).thenReturn(getCallbackRequest());
        doThrow(feignError()).when(consentOrderApprovedHandler)
            .handleConsentOrderApproved(isA(CallbackRequest.class), eq(AUTH_TOKEN));

        mvc.perform(post(consentOrderApprovedEndpoint())
            .content(requestContent.toString())
            .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void consentOrderApproved200() throws Exception {
        doValidCaseDataSetUp();

        when(finremCallbackRequestDeserializer.deserialize(any())).thenReturn(getCallbackRequest());
        when(consentOrderApprovedHandler.handleConsentOrderApproved(isA(CallbackRequest.class), eq(AUTH_TOKEN)))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        mvc.perform(post(consentOrderApprovedEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void consentInContestApproved200() throws Exception {
        doValidCaseDataSetUp();

        when(finremCallbackRequestDeserializer.deserialize(any())).thenReturn(getCallbackRequest());
        when(consentOrderApprovedHandler.handleConsentInContestConsentOrderApproved(isA(CallbackRequest.class), eq(AUTH_TOKEN)))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void consentInContestApproved400() throws Exception {
        doValidCaseDataSetUp();

        when(finremCallbackRequestDeserializer.deserialize(any())).thenReturn(getCallbackRequestEmptyCaseData());

        mvc.perform(post(contestedConsentOrderApprovedEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());

        verify(consentOrderApprovedHandler, never()).handleConsentInContestConsentOrderApproved(any(), any());
    }

    @Test
    public void sendOrder200() throws Exception {
        doValidCaseDataSetUp();

        when(finremCallbackRequestDeserializer.deserialize(any())).thenReturn(getCallbackRequest());
        when(consentOrderApprovedHandler.handleConsentInContestSendOrder(isA(CallbackRequest.class), eq(AUTH_TOKEN)))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        mvc.perform(post(contestedConsentSendOrderEndpoint())
                .content(requestContent.toString())
                .header(AUTHORIZATION_HEADER, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk());
    }
}
