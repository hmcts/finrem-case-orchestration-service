package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;

public class QuestionnairesAnswersManager extends PartyDocumentManager {

    public QuestionnairesAnswersManager(String collectionName, String party, ObjectMapper mapper) {
        super(collectionName, party, mapper);
    }

    @Override
    protected boolean isDocumentTypeValid(String caseDocumentType) {
        return caseDocumentType.equals("Questionnaire")
            || caseDocumentType.equals("Reply to Questionnaire");
    }
}
