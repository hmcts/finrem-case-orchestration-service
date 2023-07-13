package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderContestedAboutToStartHandlerTest extends UploadApprovedOrderBaseHandlerTestSetup {

    @InjectMocks
    UploadApprovedOrderContestedAboutToStartHandler uploadApprovedOrderContestedAboutToStartHandler;

    @Mock
    UploadApprovedOrderService uploadApprovedOrderService;

    @Test
    public void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenAboutToStartUploadApprovedOrder_thenHandle() {
        when(uploadApprovedOrderService.prepareFieldsForOrderApprovedCoverLetter(callbackRequest.getCaseDetails()))
            .thenReturn(caseData);
        GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> response = uploadApprovedOrderContestedAboutToStartHandler
            .handle(callbackRequest, AUTH_TOKEN);

        assertTrue(response.getData().containsKey(SUCCESS_KEY));
        verify(uploadApprovedOrderService, times(1))
            .prepareFieldsForOrderApprovedCoverLetter(callbackRequest.getCaseDetails());
    }
}
