package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;

public class QuestionnairesAnswersCollectionService extends PartyDocumentCollectionService {

    public QuestionnairesAnswersCollectionService(ManageCaseDocumentsCollectionType manageCaseDocumentsCollectionType,
                                                  CaseDocumentParty party) {
        super(manageCaseDocumentsCollectionType, party);
    }

    @Override
    protected boolean canProcessDocumentType(CaseDocumentType caseDocumentType) {
        return caseDocumentType.equals(CaseDocumentType.QUESTIONNAIRE)
            || caseDocumentType.equals(CaseDocumentType.REPLY_TO_QUESTIONNAIRE);
    }
}
