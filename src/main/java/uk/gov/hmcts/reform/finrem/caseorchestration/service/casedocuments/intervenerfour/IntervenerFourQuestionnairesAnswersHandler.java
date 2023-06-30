package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerFourQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerFourQuestionnairesAnswersHandler() {
        super(INTV_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_FOUR);
    }
}
