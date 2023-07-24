package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

import java.util.List;

@Service
public class FdrDocumentsHandler extends DocumentHandler {


    public FdrDocumentsHandler() {
        super(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION);
    }

    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream().filter(this::isFdr).toList();
    }

    private boolean isFdr(UploadCaseDocumentCollection managedDocumentCollection) {
        UploadCaseDocument uploadedCaseDocument = managedDocumentCollection.getUploadCaseDocument();
        return !isIntervener(uploadedCaseDocument.getCaseDocumentParty())
            && YesOrNo.isNoOrNull(uploadedCaseDocument.getCaseDocumentConfidential())
            && YesOrNo.isYes(uploadedCaseDocument.getCaseDocumentFdr());
    }

    private boolean isIntervener(CaseDocumentParty caseDocumentParty) {
        return CaseDocumentParty.INTERVENER_ONE.equals(caseDocumentParty)
            || CaseDocumentParty.INTERVENER_TWO.equals(caseDocumentParty)
            || CaseDocumentParty.INTERVENER_THREE.equals(caseDocumentParty)
            || CaseDocumentParty.INTERVENER_FOUR.equals(caseDocumentParty);
    }
}
