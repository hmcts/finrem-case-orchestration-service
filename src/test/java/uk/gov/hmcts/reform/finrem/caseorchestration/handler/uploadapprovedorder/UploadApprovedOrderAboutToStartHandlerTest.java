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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderAboutToStartHandlerTest extends UploadApprovedOrderBaseHandlerTest {

    @InjectMocks
    UploadApprovedOrderAboutToStartHandler uploadApprovedOrderAboutToStartHandler;

    @Mock
    UploadApprovedOrderService uploadApprovedOrderService;

    @Test
    public void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderAboutToStartHandler
            .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenHandle() {
        when(uploadApprovedOrderService.handleLatestDraftDirectionOrder(callbackRequest.getCaseDetails()))
            .thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = uploadApprovedOrderAboutToStartHandler
            .handle(callbackRequest, AUTH_TOKEN);

        assertTrue(response.getData().containsKey(SUCCESS_KEY));
        verify(uploadApprovedOrderService, times(1))
            .handleLatestDraftDirectionOrder(callbackRequest.getCaseDetails());
    }
}
