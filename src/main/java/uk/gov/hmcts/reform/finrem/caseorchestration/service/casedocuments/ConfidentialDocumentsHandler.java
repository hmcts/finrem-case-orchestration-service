package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Service
public class ConfidentialDocumentsHandler extends DocumentHandler {

    public ConfidentialDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONFIDENTIAL_DOCS_COLLECTION, featureToggleService);
    }

    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(managedDocumentCollection ->
                YesOrNo.isYes(managedDocumentCollection.getUploadCaseDocument().getCaseDocumentConfidentiality()))
            .toList();
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.CONFIDENTIAL_DOCUMENTS;
    }
}
