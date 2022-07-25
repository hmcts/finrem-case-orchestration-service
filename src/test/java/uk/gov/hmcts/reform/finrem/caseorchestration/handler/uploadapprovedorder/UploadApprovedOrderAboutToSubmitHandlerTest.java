package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderAboutToSubmitHandlerTest extends UploadApprovedOrderBaseHandlerTest {

    @InjectMocks
    UploadApprovedOrderAboutToSubmitHandler uploadApprovedOrderAboutToSubmitHandler;

    @Mock
    UploadApprovedOrderService uploadApprovedOrderService;

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderAboutToSubmitHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrderAndNoErrors_thenHandle() {
        when(uploadApprovedOrderService
            .handleUploadApprovedOrderAboutToSubmit(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build());

        AboutToStartOrSubmitCallbackResponse response = uploadApprovedOrderAboutToSubmitHandler
            .handle(callbackRequest, AUTH_TOKEN);

        assertTrue(response.getData().containsKey(SUCCESS_KEY));
        verify(uploadApprovedOrderService, times(1))
            .handleUploadApprovedOrderAboutToSubmit(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrderAndErrors_thenHandle() {
        when(uploadApprovedOrderService
            .handleUploadApprovedOrderAboutToSubmit(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().errors(List.of("ERROR")).build());

        AboutToStartOrSubmitCallbackResponse response = uploadApprovedOrderAboutToSubmitHandler
            .handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getErrors(), hasSize(1));
        assertEquals("ERROR", response.getErrors().get(0));
        verify(uploadApprovedOrderService, times(1))
            .handleUploadApprovedOrderAboutToSubmit(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }
}
