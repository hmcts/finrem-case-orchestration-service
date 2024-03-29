package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION;

@Component
public class IntervenerOneQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    public IntervenerOneQuestionnairesAnswersHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_QUESTIONNAIRES_ANSWERS_COLLECTION, INTERVENER_ONE, featureToggleService);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType, CaseDocumentParty caseDocumentParty) {
        switch (caseDocumentType) {
            case REPLY_TO_QUESTIONNAIRE -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_REPLIES_TO_QUESTIONNAIRE;
            }
            case QUESTIONNAIRE -> {
                return DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_QUESTIONNAIRES;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
