package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

public abstract class UpdatingDisclosureHandler extends PartyDocumentsHandler {

    private final DocumentCategory documentCategory;

    protected UpdatingDisclosureHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                        CaseDocumentParty party, DocumentCategory documentCategory,
                                        FeatureToggleService featureToggleService) {
        super(caseDocumentCollectionType, party, featureToggleService);
        this.documentCategory = documentCategory;
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {

        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && (caseDocumentType.equals(CaseDocumentType.UPDATING_DISCLOSURE));
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(
            CaseDocumentType caseDocumentType,
            CaseDocumentParty caseDocumentParty) {

        return CaseDocumentType.UPDATING_DISCLOSURE.equals(caseDocumentType)
                ? documentCategory
                : DocumentCategory.UNCATEGORISED;
    }

}
