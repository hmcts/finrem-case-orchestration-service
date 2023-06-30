package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

@Service
public class RespondentQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public RespondentQuestionnairesAnswersHandler() {
        super(ManageCaseDocumentsCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION,
            CaseDocumentParty.RESPONDENT);
    }
}
