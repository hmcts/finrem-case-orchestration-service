package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerOneChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public IntervenerOneChronologiesStatementHandler() {
        super(INTV_ONE_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_ONE);
    }
}
