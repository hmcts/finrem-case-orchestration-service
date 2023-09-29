package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;

public abstract class QuestionnairesAnswersHandler extends PartyDocumentsHandler {

    public QuestionnairesAnswersHandler(CaseDocumentCollectionType caseDocumentCollectionType,
                                        CaseDocumentParty party) {
        super(caseDocumentCollectionType, party);
    }

    @Override
    protected boolean canHandleDocument(UploadCaseDocument uploadCaseDocument) {
        CaseDocumentType caseDocumentType = uploadCaseDocument.getCaseDocumentType();
        return uploadCaseDocument.getCaseDocumentFdr().equals(YesOrNo.NO)
            && (caseDocumentType.equals(CaseDocumentType.QUESTIONNAIRE)
            || caseDocumentType.equals(CaseDocumentType.REPLY_TO_QUESTIONNAIRE));
    }
}
