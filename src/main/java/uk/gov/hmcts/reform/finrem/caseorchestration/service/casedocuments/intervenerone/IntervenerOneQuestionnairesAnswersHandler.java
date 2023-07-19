package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerOneQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerOneQuestionnairesAnswersHandler(ObjectMapper mapper) {
        super(INTV_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_ONE, mapper);
    }
}
