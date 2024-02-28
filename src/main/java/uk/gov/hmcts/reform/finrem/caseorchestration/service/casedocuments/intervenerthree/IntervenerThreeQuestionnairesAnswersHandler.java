package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerThreeQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    public IntervenerThreeQuestionnairesAnswersHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_THREE_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_THREE, featureToggleService);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        switch (caseDocumentType) {
            case REPLY_TO_QUESTIONNAIRE -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_REPLIES_TO_QUESTIONNAIRE;
            }
            case QUESTIONNAIRE -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_QUESTIONNAIRES;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
