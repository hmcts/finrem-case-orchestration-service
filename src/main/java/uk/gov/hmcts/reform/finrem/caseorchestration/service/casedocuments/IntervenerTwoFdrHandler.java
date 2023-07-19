package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

@Service
public class IntervenerTwoFdrHandler extends PartyDocumentsHandler {

    public IntervenerTwoFdrHandler() {
        super(CaseDocumentCollectionType.INTERVENER_TWO_FDR_DOCS_COLLECTION, CaseDocumentParty.INTERVENER_TWO);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.YES);
    }
}
