package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
public class ConfidentialDocumentsHandler extends DocumentHandler {

    public ConfidentialDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONFIDENTIAL_DOCS_COLLECTION, featureToggleService);
    }

    @Override
    protected List<UploadCaseDocumentCollection> getAlteredCollectionForType(
        List<UploadCaseDocumentCollection> allManagedDocumentCollections) {

        return allManagedDocumentCollections.stream()
            .filter(managedDocumentCollection ->
                YesOrNo.isYes(managedDocumentCollection.getUploadCaseDocument().getCaseDocumentConfidentiality()))
            .toList();
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        if (caseDocumentParty == null) {
            return DocumentCategory.CONFIDENTIAL_DOCUMENTS;
        }

        return switch (caseDocumentParty) {
            case APPLICANT -> DocumentCategory.CONFIDENTIAL_DOCUMENTS_APPLICANT;
            case RESPONDENT -> DocumentCategory.CONFIDENTIAL_DOCUMENTS_RESPONDENT;
            case INTERVENER_ONE -> DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_1;
            case INTERVENER_TWO -> DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_2;
            case INTERVENER_THREE -> DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_3;
            case INTERVENER_FOUR -> DocumentCategory.CONFIDENTIAL_DOCUMENTS_INTERVENER_4;
            default -> DocumentCategory.CONFIDENTIAL_DOCUMENTS;
        };
    }
}
