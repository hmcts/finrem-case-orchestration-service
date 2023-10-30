package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerTwoQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    public IntervenerTwoQuestionnairesAnswersHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_TWO_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_TWO, featureToggleService);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case REPLY_TO_QUESTIONNAIRE -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_REPLIES_TO_QUESTIONNAIRE;
            }
            case QUESTIONNAIRE -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_2_QUESTIONNAIRES;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
