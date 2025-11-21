package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentcatergory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoryAssigner;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AssignDocumentCategoriesAboutToSubmitHandlerTest {

    @InjectMocks
    private AssignDocumentCategoriesAboutToSubmitHandler handler;

    @Mock
    DocumentCategoryAssigner documentCategoryAssigner;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.ASSIGN_DOCUMENT_CATEGORIES);
    }

    @Test
    void shouldCallAssignDocumentCategories() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from();
        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(documentCategoryAssigner).assignDocumentCategories(callbackRequest.getCaseDetails().getData());
    }
}
