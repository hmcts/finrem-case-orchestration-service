package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerFourQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerFourQuestionnairesAnswersHandler(ObjectMapper mapper) {
        super(INTV_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_FOUR, mapper);
    }
}
