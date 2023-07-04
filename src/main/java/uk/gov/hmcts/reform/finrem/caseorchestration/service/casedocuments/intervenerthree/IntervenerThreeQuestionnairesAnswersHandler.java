package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerThreeQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerThreeQuestionnairesAnswersHandler(ObjectMapper mapper) {
        super(INTV_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_THREE, mapper);
    }
}
