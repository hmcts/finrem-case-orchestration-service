package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerOneQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerOneQuestionnairesAnswersHandler() {
        super(INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_ONE);
    }
}
