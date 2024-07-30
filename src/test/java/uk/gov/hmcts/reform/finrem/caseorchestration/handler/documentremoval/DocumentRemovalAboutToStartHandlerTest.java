package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentremoval;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentcatergory.AssignDocumentCategoriesAboutToSubmitHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalAboutToStartHandlerTest {

    @InjectMocks
    private AssignDocumentCategoriesAboutToSubmitHandler handler;

    @Test
    void shouldHandleEventContested() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.REMOVE_CASE_DOCUMENT));
    }

    @Test
    void shouldHandleEventConsented() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REMOVE_CASE_DOCUMENT));
    }
    @Test
    public void shouldGenerateDocumentListFromCaseData() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.create();
        handler.handle(callbackRequest, "authToken");
    }


}
