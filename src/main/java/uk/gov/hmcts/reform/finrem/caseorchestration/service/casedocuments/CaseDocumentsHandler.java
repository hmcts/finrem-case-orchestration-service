package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

@Component
public class CaseDocumentsHandler extends PartyDocumentsHandler {

    public CaseDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONTESTED_UPLOADED_DOCUMENTS,
            CaseDocumentParty.CASE, featureToggleService);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return true;
    }


    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case ATTENDANCE_SHEETS, JUDICIAL_NOTES, WITNESS_SUMMONS -> {
                return DocumentCategory.ADMINISTRATIVE_DOCUMENTS;
            }
            case JUDGMENT, TRANSCRIPT -> {
                return DocumentCategory.JUDGMENT_OR_TRANSCRIPT;
            }
            default -> {
                return DocumentCategory.CASE_DOCUMENTS;
            }
        }
    }
}
