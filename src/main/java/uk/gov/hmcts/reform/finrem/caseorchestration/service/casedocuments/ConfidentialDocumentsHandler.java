package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfidentialDocumentsHandler extends DocumentHandler {

    public ConfidentialDocumentsHandler() {
        super(ManageCaseDocumentsCollectionType.CONFIDENTIAL_DOCS_COLLECTION);
    }

    protected List<UploadCaseDocumentCollection> getTypedManagedDocumentCollections(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(managedDocumentCollection ->
                YesOrNo.isYes(managedDocumentCollection.getUploadCaseDocument().getCaseDocumentConfidential()))
            .collect(Collectors.toList());
    }
}
