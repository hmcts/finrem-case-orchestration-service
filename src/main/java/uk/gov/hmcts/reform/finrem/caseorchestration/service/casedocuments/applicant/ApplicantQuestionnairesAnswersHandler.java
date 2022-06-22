package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class ApplicantQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public ApplicantQuestionnairesAnswersHandler(ObjectMapper mapper) {
        super(APP_QUESTIONNAIRES_ANSWERS_COLLECTION, APPLICANT, mapper);
    }
}
