package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

public abstract class PartyFdrDocumentCategoriser {

    public DocumentCategory getDocumentCategory(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case POSITION_STATEMENT:
                return getPositionStatementsDocumentCategory();
            case ES1:
                return DocumentCategory.FDR_JOINT_DOCUMENTS_ES1;
            case ES2:
                return DocumentCategory.FDR_JOINT_DOCUMENTS_ES2;
            case CHRONOLOGY:
                return DocumentCategory.FDR_JOINT_DOCUMENTS_CHRONOLOGY;
            case WITHOUT_PREJUDICE_OFFERS:
                return getWithoutPrejudiceDocumentCategory();
            case QUESTIONNAIRE:
                return getQuestionnairesDocumentCategory();
            case FAMILY_HOME_VALUATION, PENSION_REPORT, EXPERT_EVIDENCE:
                return DocumentCategory.FDR_REPORTS;
            case PRE_HEARING_DRAFT_ORDER:
                return getHearingDraftOrderDocumentCategory();
            case SKELETON_ARGUMENT:
                return getSkeletonArgumentDocumentCategory();
            default:
                return getDefaultDocumentCategory();
        }
    }

    protected abstract DocumentCategory getDefaultDocumentCategory();

    protected abstract DocumentCategory getSkeletonArgumentDocumentCategory();

    protected abstract DocumentCategory getHearingDraftOrderDocumentCategory();

    protected abstract DocumentCategory getQuestionnairesDocumentCategory();

    protected abstract DocumentCategory getWithoutPrejudiceDocumentCategory();

    protected abstract DocumentCategory getPositionStatementsDocumentCategory();
}
