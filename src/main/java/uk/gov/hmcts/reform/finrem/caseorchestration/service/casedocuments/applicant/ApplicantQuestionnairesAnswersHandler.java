package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

@Service
public class ApplicantQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    @Autowired
    public ApplicantQuestionnairesAnswersHandler() {
        super(CaseDocumentCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
