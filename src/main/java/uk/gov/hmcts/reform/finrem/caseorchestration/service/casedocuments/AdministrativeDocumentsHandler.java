package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

public class AdministrativeDocumentsHandler extends PartyDocumentsHandler {

    private static List<CaseDocumentType> administrativeCaseDocumentTypes = List.of(
        CaseDocumentType.ATTENDANCE_SHEETS,
        CaseDocumentType.JUDICIAL_NOTES,
        CaseDocumentType.JUDGMENT,
        CaseDocumentType.WITNESS_SUMMONS,
        CaseDocumentType.TRANSCRIPT
    );

    public AdministrativeDocumentsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.CONTESTED_UPLOADED_DOCUMENTS, CaseDocumentParty.CASE, featureToggleService);
    }


    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        if (administrativeCaseDocumentTypes.contains(uploadCaseDocument.getCaseDocumentType())) {
            uploadCaseDocument.setCaseDocumentParty(CaseDocumentParty.CASE);
            uploadCaseDocument.setCaseDocumentConfidentiality(YesOrNo.NO);
            return true;
        }
        return false;
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.CASE_DOCUMENTS;
    }
}
