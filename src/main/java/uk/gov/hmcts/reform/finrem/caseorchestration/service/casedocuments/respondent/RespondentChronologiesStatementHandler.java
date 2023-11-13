package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

@Service
public class RespondentChronologiesStatementHandler extends ChronologiesStatementsHandler {

    public RespondentChronologiesStatementHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION,
            CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case CHRONOLOGY -> {
                return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_CHRONOLOGY;
            }
            case STATEMENT_OF_ISSUES -> {
                return DocumentCategory.RESPONDENT_DOCUMENTS_CONCISE_STATEMENT_OF_ISSUES;
            }
            case FORM_G -> {
                return DocumentCategory.RESPONDENT_DOCUMENTS_FORM_G;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
