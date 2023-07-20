package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.List;

@Service
public class ConfidentialDocumentsHandler extends DocumentHandler {

    public ConfidentialDocumentsHandler() {
        super(CaseDocumentCollectionType.CONFIDENTIAL_DOCS_COLLECTION);
    }

    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(managedDocumentCollection ->
                YesOrNo.isYes(managedDocumentCollection.getUploadCaseDocument().getCaseDocumentConfidential()))
            .toList();
    }
}
