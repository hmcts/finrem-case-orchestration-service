package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentsHandler extends DocumentHandler {
    private final CaseDocumentParty party;

    public PartyDocumentsHandler(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                 CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType);
        this.party = party;
    }

    protected abstract boolean canHandleDocument(UploadCaseDocument uploadCaseDocument);

    protected List<UploadCaseDocumentCollection> getTypedManagedDocumentCollections(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(d -> {
                UploadCaseDocument uploadedCaseDocument = d.getUploadCaseDocument();
                return uploadedCaseDocument.getCaseDocuments() != null
                    && uploadedCaseDocument.getCaseDocumentParty() != null
                    && uploadedCaseDocument.getCaseDocumentType() != null
                    && uploadedCaseDocument.getCaseDocumentParty().equals(party)
                    && uploadedCaseDocument.getCaseDocumentConfidential().equals(YesOrNo.NO);
            })
            .filter(d -> canHandleDocument(d.getUploadCaseDocument()))
            .collect(Collectors.toList());
    }
}
