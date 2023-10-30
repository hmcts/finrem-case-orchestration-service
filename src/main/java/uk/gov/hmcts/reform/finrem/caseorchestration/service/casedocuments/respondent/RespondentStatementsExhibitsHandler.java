package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

@Service
public class RespondentStatementsExhibitsHandler extends StatementExhibitsHandler {

    public RespondentStatementsExhibitsHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION,
                CaseDocumentParty.RESPONDENT, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.RESPONDENT_DOCUMENTS_S25_STATEMENT;
            }
            case WITNESS_STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.RESPONDENT_DOCUMENTS_WITNESS_STATEMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
