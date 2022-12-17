package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersCollectionService;

@Service
public class RespondentQuestionnairesAnswersCollectionService extends QuestionnairesAnswersCollectionService {

    @Autowired
    public RespondentQuestionnairesAnswersCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION,
            CaseDocumentParty.RESPONDENT);
    }
}
