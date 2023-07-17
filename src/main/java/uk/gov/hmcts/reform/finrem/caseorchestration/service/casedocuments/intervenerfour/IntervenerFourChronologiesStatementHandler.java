package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerFourChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public IntervenerFourChronologiesStatementHandler() {
        super(INTERVENER_FOUR_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_FOUR);
    }
}
