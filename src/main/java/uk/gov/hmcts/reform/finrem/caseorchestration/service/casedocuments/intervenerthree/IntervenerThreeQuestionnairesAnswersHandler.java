package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerThreeQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerThreeQuestionnairesAnswersHandler() {
        super(INTV_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_THREE);
    }
}
