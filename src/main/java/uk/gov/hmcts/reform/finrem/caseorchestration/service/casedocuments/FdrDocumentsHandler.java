package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
@Order(1)
public class FdrDocumentsHandler extends DocumentHandler {

    public FdrDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONTESTED_FDR_CASE_DOCUMENT_COLLECTION, featureToggleService);
    }

    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream().filter(this::isWithoutPrejudiceOrFdr).toList();
    }

    private boolean isWithoutPrejudiceOrFdr(UploadCaseDocumentCollection uploadCaseDocumentCollection) {
        return isWithoutPrejudice(uploadCaseDocumentCollection) || isFdr(uploadCaseDocumentCollection);
    }

    private boolean isWithoutPrejudice(UploadCaseDocumentCollection managedDocumentCollection) {
        UploadCaseDocument uploadedCaseDocument = managedDocumentCollection.getUploadCaseDocument();
        if (CaseDocumentType.WITHOUT_PREJUDICE_OFFERS.equals(uploadedCaseDocument.getCaseDocumentType())) {
            uploadedCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            uploadedCaseDocument.setCaseDocumentFdr(YesOrNo.YES);
            uploadedCaseDocument.setCaseDocumentParty(null);
            return true;
        }
        return false;
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
        return CaseDocumentType.VALUATION_REPORT.equals(caseDocumentType)
            ? DocumentCategory.REPORTS : DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE;
    }
}
