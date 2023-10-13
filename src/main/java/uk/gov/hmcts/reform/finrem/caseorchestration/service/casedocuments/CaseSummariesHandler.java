package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

public class CaseSummariesHandler extends PartyDocumentsHandler {

    public CaseSummariesHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                CaseDocumentParty party, FeatureToggleService featureToggleService) {
        super(caseDocumentCollectionType, party, featureToggleService);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {

        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && (caseDocumentType.equals(CaseDocumentType.POSITION_STATEMENT)
            || caseDocumentType.equals(CaseDocumentType.SKELETON_ARGUMENT)
            || caseDocumentType.equals(CaseDocumentType.CASE_SUMMARY));
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.HEARING_DOCUMENTS;
    }
}
