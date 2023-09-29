package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

@Service
public class ApplicantQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    public ApplicantQuestionnairesAnswersHandler() {
        super(CaseDocumentCollectionType.APP_QUESTIONNAIRES_ANSWERS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case REPLY_TO_QUESTIONNAIRE -> {
                return DocumentCategory.APPLICANT_DOCUMENTS;
            }
            case QUESTIONNAIRE -> {
                return DocumentCategory.HEARING_DOCUMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
