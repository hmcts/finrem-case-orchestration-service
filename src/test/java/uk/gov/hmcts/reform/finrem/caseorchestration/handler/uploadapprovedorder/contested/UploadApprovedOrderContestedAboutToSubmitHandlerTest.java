package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.helper.DocumentWarningsHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UploadApprovedOrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.UPLOAD_APPROVED_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class UploadApprovedOrderContestedAboutToSubmitHandlerTest {

    @InjectMocks
    private UploadApprovedOrderContestedAboutToSubmitHandler underTest;

    @Mock
    private UploadApprovedOrderService uploadApprovedOrderService;

    @Mock
    private DocumentWarningsHelper documentWarningsHelper;

    @Test
    void canHandle() {
        assertCanHandle(underTest, ABOUT_TO_SUBMIT, CONTESTED, UPLOAD_APPROVED_ORDER);
    }

    @Test
    void givenContestedCase_whenAboutToSubmitUploadApprovedOrderAndNoErrors_thenHandle() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(Long.valueOf(CASE_ID), CONTESTED, UPLOAD_APPROVED_ORDER);
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertTrue(response.getErrors().isEmpty());
        verify(uploadApprovedOrderService).processApprovedOrders(finremCallbackRequest, new ArrayList<>(), AUTH_TOKEN);
    }

    @Test
    void givenContestedCase_whenUploadDocumentWithWarnings_thenReturnWarnings() {
        FinremCallbackRequest finremCallbackRequest = FinremCallbackRequestFactory
            .from(Long.valueOf(CASE_ID), CONTESTED, UPLOAD_APPROVED_ORDER);
        when(documentWarningsHelper.getDocumentWarnings(eq(finremCallbackRequest), any(Function.class), eq(AUTH_TOKEN)))
            .thenReturn(List.of("warning 1"));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = underTest.handle(finremCallbackRequest, AUTH_TOKEN);
        assertThat(response.getWarnings()).containsExactly("warning 1");

        verify(uploadApprovedOrderService).processApprovedOrders(finremCallbackRequest, new ArrayList<>(), AUTH_TOKEN);
        verify(documentWarningsHelper).getDocumentWarnings(eq(finremCallbackRequest), any(Function.class), eq(AUTH_TOKEN));
    }
}
