package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadAdditionalDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
public class FormADocumentCategorizer extends DocumentCategorizer {

    @Autowired
    public FormADocumentCategorizer(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categorizeDocuments(FinremCaseData finremCaseData) {

        List<UploadAdditionalDocumentCollection> uploadAdditionDocumentsList = finremCaseData.getUploadAdditionalDocument();
        if (uploadAdditionDocumentsList != null && !uploadAdditionDocumentsList.isEmpty()) {
            for (UploadAdditionalDocumentCollection uploadAdditionalDocument : uploadAdditionDocumentsList) {
                if (uploadAdditionalDocument.getValue().getAdditionalDocuments() != null) {
                    uploadAdditionalDocument.getValue().getAdditionalDocuments().setCategoryId(
                        DocumentCategory.APPLICATIONS_FORM_A_OR_A1_OR_B.getDocumentCategoryId());
                }
            }
        }

    }
}
