package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DocumentCategoryAssignerTest {

    DocumentCategoryAssigner documentCategoryAssigner;
    List<DocumentCategoriser> documentCategorisers = new ArrayList<>();
    List<DocumentHandler> documentHandlers = new ArrayList<>();

    @Before
    public void setUpTest() {
        documentCategorisers.add(Mockito.mock(DocumentCategoriser.class));
        documentHandlers.add(Mockito.mock(DocumentHandler.class));
        documentCategoryAssigner = new DocumentCategoryAssigner(documentCategorisers, documentHandlers);
    }

    @Test
    public void assignDocumentCategories() {
        documentCategoryAssigner.assignDocumentCategories(FinremCaseData.builder().build());
        Mockito.verify(documentCategorisers.get(0), Mockito.times(1)).categorise(Mockito.any(FinremCaseData.class));
        Mockito.verify(documentHandlers.get(0), Mockito.times(1))
            .assignDocumentCategoryToUploadDocumentsCollection(Mockito.any(FinremCaseData.class));
    }
}
