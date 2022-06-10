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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DIRECTION_ORDER_IS_FINAL;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderMidEventHandlerTest extends UploadApprovedOrderBaseHandlerTest {

    @InjectMocks
    UploadApprovedOrderMidEventHandler uploadApprovedOrderMidEventHandler;

    @Mock
    UploadApprovedOrderService uploadApprovedOrderService;

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderMidEventHandler
                .canHandle(CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderMidEventHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenMidEventUploadApprovedOrderAndNoErrors_thenHandle() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(LATEST_DIRECTION_ORDER_IS_FINAL, YES_VALUE);
        when(uploadApprovedOrderService.setIsFinalHearingFieldMidEvent(callbackRequest.getCaseDetails()))
            .thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = uploadApprovedOrderMidEventHandler
            .handle(callbackRequest, AUTH_TOKEN);

        assertTrue(response.getData().containsKey(LATEST_DIRECTION_ORDER_IS_FINAL));
        assertEquals(response.getData().get(LATEST_DIRECTION_ORDER_IS_FINAL), YES_VALUE);
        verify(uploadApprovedOrderService, times(1))
            .setIsFinalHearingFieldMidEvent(callbackRequest.getCaseDetails());
    }
}
