package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

public class FormsHHandler extends PartyDocumentsHandler {

    public FormsHHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                         CaseDocumentParty party) {
        super(caseDocumentCollectionType, party);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && uploadCaseDocument.getCaseDocumentType().equals(CaseDocumentType.FORM_H);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        return DocumentCategory.HEARING_DOCUMENTS;
    }
}
