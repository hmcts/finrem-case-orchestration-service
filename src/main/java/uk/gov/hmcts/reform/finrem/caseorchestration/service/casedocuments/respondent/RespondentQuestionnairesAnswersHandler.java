package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class RespondentQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public RespondentQuestionnairesAnswersHandler(ObjectMapper mapper) {
        super(RESP_QUESTIONNAIRES_ANSWERS_COLLECTION, RESPONDENT, mapper);
    }
}
