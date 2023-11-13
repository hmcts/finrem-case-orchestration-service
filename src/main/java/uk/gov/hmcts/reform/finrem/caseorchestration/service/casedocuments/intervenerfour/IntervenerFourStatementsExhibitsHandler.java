package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerFourStatementsExhibitsHandler extends StatementExhibitsHandler {

    public IntervenerFourStatementsExhibitsHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_FOUR, featureToggleService);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_S25_STATEMENT;
            }
            case WITNESS_STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_WITNESS_STATEMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
