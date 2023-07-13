package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

@Service
public class CaseDocumentsHandler extends PartyDocumentsHandler {

    @Autowired
    public CaseDocumentsHandler() {
        super(CaseDocumentCollectionType.CONTESTED_UPLOADED_DOCUMENTS,
            CaseDocumentParty.CASE);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return true;
    }
}
