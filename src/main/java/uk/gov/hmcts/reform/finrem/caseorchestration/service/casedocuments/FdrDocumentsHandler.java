package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Service
public class FdrDocumentsHandler extends DocumentHandler {

    private final FeatureToggleService featureToggleService;

    public FdrDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION, featureToggleService);
        this.featureToggleService = featureToggleService;
    }

    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream().filter(this::isFdr).toList();
    }

    private boolean isFdr(UploadCaseDocumentCollection managedDocumentCollection) {
        UploadCaseDocument uploadedCaseDocument = managedDocumentCollection.getUploadCaseDocument();
        return !isIntervener(uploadedCaseDocument.getCaseDocumentParty())
            && YesOrNo.isNoOrNull(uploadedCaseDocument.getCaseDocumentConfidentiality())
            && YesOrNo.isYes(uploadedCaseDocument.getCaseDocumentFdr());
    }

    private boolean isIntervener(CaseDocumentParty caseDocumentParty) {
        return CaseDocumentParty.INTERVENER_ONE.equals(caseDocumentParty)
            || CaseDocumentParty.INTERVENER_TWO.equals(caseDocumentParty)
            || CaseDocumentParty.INTERVENER_THREE.equals(caseDocumentParty)
            || CaseDocumentParty.INTERVENER_FOUR.equals(caseDocumentParty);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE;
    }
}
