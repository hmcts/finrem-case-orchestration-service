package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.QuestionnairesAnswersHandler;

@Component
public class RespondentQuestionnairesAnswersHandler extends QuestionnairesAnswersHandler {

    public RespondentQuestionnairesAnswersHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_QUESTIONNAIRES_ANSWERS_COLLECTION,
            CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    public DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case REPLY_TO_QUESTIONNAIRE -> {
                return DocumentCategory.RESPONDENT_DOCUMENTS_REPLIES_TO_QUESTIONNAIRE;
            }
            case QUESTIONNAIRE -> {
                return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_QUESTIONNAIRES;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
