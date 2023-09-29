package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

public class HearingBundleHandler extends PartyDocumentsHandler {

    public HearingBundleHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                CaseDocumentParty party) {
        super(caseDocumentCollectionType, party);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {

        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && caseDocumentType.equals(CaseDocumentType.TRIAL_BUNDLE);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.HEARING_BUNDLE;
//        TODO - Check if this is correct category (Looks like it from name of Handler and category name)
    }
}
