package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerOneChronologiesStatementHandler extends ChronologiesStatementsHandler {

    public IntervenerOneChronologiesStatementHandler() {
        super(INTERVENER_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_ONE);
    }

    @Override
    protected DocumentCategory getDocumentCategoryFromDocumentType(CaseDocumentType caseDocumentType) {
        switch (caseDocumentType) {
            case CHRONOLOGY -> {
                return DocumentCategory.HEARING_DOCUMENTS;
            }
            case STATEMENT_OF_ISSUES, FORM_G -> {
                return DocumentCategory.INTERVENER_DOCUMENTS;
            }
            default -> {
                return DocumentCategory.UNCATEGORISED;
            }
        }
    }
}
