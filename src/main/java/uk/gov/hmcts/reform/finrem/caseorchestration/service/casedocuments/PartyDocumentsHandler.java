package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class PartyDocumentsHandler extends DocumentHandler {
    private final CaseDocumentParty party;

    public PartyDocumentsHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                 CaseDocumentParty party) {
        super(caseDocumentCollectionType);
        this.party = party;
    }

    protected abstract boolean canHandleDocument(UploadCaseDocument uploadCaseDocument);

    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(this::isNonConfidentialDocWithParty)
            .filter(d -> canHandleDocument(d.getUploadCaseDocument()))
            .collect(Collectors.toList());
    }

    private boolean isNonConfidentialDocWithParty(UploadCaseDocumentCollection d) {
        UploadCaseDocument uploadedCaseDocument = d.getUploadCaseDocument();
        return party.equals(uploadedCaseDocument.getCaseDocumentParty())
            && YesOrNo.isNoOrNull(uploadedCaseDocument.getCaseDocumentConfidential());
    }
}
