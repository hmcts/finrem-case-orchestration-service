package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerTwoQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public IntervenerTwoQuestionnairesAnswersHandler(ObjectMapper mapper) {
        super(INTV_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_TWO, mapper);
    }
}
