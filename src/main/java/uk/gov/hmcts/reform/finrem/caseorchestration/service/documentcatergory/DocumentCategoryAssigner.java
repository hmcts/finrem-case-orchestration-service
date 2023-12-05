package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.DocumentHandler;

import java.util.List;

@Component
@AllArgsConstructor
public class DocumentCategoryAssigner {

    private final List<DocumentCategoriser> documentCategorisers;
    private final List<DocumentHandler> documentHandlers;

    public void assignDocumentCategories(FinremCaseData finremCaseData) {
        documentCategorisers.forEach(documentCategoriser -> documentCategoriser.categorise(finremCaseData));
        documentHandlers.forEach(documentHandler -> documentHandler.assignDocumentCategoryToUploadDocumentsCollection(finremCaseData));
    }
}
