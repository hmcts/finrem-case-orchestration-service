package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

@Component
public class RespondentFdrDocumentCategoriser {

    public DocumentCategory getDocumentCategory(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case POSITION_STATEMENT:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_POSITION_STATEMENTS;
            case ES1:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_ES1;
            case ES2:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_ES2;
            case WITHOUT_PREJUDICE_OFFERS:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_WITHOUT_PREJUDICE_OFFERS;
            case CHRONOLOGY:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_CHRONOLOGY;
            case QUESTIONNAIRE:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_QUESTIONNAIRES;
            case FAMILY_HOME_VALUATION:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_FAMILY_HOME_VALUATION;
            case PRE_HEARING_DRAFT_ORDER:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_PRE_HEARING_DRAFT_ORDER;
            case SKELETON_ARGUMENT:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT_SKELETON_ARGUMENT;
            case OTHER:
                return DocumentCategory.FDR_BUNDLE;
            default:
                return DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE_RESPONDENT;
        }
    }
}
