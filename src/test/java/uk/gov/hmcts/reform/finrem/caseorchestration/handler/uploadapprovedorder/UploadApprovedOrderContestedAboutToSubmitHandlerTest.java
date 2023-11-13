package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderContestedAboutToSubmitHandlerTest extends UploadApprovedOrderBaseHandlerTestSetup {

    @InjectMocks
    UploadApprovedOrderContestedAboutToSubmitHandler handler;

    @Mock
    UploadApprovedOrderService service;

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCanHandle() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.UPLOAD_APPROVED_ORDER));
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER));
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE));
    }

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrderAndNoErrors_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest(EventType.UPLOAD_APPROVED_ORDER);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(finremCallbackRequest, AUTH_TOKEN);
        assertTrue(response.getErrors().isEmpty());
        verify(service).processApprovedOrders(finremCallbackRequest, new ArrayList<>(), AUTH_TOKEN);
    }
}
