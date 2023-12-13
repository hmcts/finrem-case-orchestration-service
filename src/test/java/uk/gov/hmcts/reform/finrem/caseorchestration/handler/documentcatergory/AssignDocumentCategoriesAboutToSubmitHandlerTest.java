package uk.gov.hmcts.reform.finrem.caseorchestration.handler.documentcatergory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoryAssigner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AssignDocumentCategoriesAboutToSubmitHandlerTest {

    @InjectMocks
    private AssignDocumentCategoriesAboutToSubmitHandler handler;

    @Mock
    DocumentCategoryAssigner documentCategoryAssigner;

    @Test
    public void shouldHandleEvent() {
        assertTrue(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.ASSIGN_DOCUMENT_CATEGORIES));
    }

    @Test
    public void shouldNotHandleEvent() {
        assertFalse(handler.canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.ASSIGN_DOCUMENT_CATEGORIES));
    }

    @Test
    public void shouldCallAssignDocumentCategories() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCallbackRequest callbackRequest =
            FinremCallbackRequest.builder().caseDetails(FinremCaseDetails.builder().data(caseData).build()).build();
        handler.handle(callbackRequest, "authToken");
        verify(documentCategoryAssigner).assignDocumentCategories(caseData);
    }

}
