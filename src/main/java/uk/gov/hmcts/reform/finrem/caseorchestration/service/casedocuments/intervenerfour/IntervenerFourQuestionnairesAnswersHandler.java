package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerFourQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    public IntervenerFourQuestionnairesAnswersHandler() {
        super(INTERVENER_FOUR_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_FOUR);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case REPLY_TO_QUESTIONNAIRE -> {
                return DocumentCategory.INTERVENER_DOCUMENTS;
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
