package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FdrDocumentsHandler extends DocumentHandler {


    public FdrDocumentsHandler() {
        super(ManageCaseDocumentsCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION);
    }

    protected List<UploadCaseDocumentCollection> getTypedManagedDocumentCollections(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(managedDocumentCollection -> {
                UploadCaseDocument uploadedCaseDocument = managedDocumentCollection.getUploadCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentFdr() != null
                    && !isIntervener(uploadedCaseDocument.getCaseDocumentParty())
                    && uploadedCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
            }).collect(Collectors.toList());
    }
}
