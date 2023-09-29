package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerThreeStatementsExhibitsHandler extends StatementExhibitsHandler {

    public IntervenerThreeStatementsExhibitsHandler() {
        super(INTERVENER_THREE_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_THREE);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case STATEMENT_AFFIDAVIT, WITNESS_STATEMENT_AFFIDAVIT -> {
                return DocumentCategory.INTERVENER_DOCUMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
