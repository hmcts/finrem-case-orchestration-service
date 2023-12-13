package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerOneStatementsExhibitsHandler extends StatementExhibitsHandler {

    public IntervenerOneStatementsExhibitsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_ONE, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_S25_STATEMENT;
            }
            case WITNESS_STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_WITNESS_STATEMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
