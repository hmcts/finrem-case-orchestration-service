package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

public class QuestionnairesAnswersHandler extends PartyDocumentHandler {

    public QuestionnairesAnswersHandler(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                        CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.QUESTIONNAIRE)
            || caseDocumentType.equals(CaseDocumentType.REPLY_TO_QUESTIONNAIRE);
    }
}
