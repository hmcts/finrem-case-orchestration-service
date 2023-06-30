package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerthree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_THREE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION;

@Component
public class IntervenerThreeChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public IntervenerThreeChronologiesStatementHandler() {
        super(INTV_THREE_CHRONOLOGIES_STATEMENTS_COLLECTION, INTERVENER_THREE);
    }
}
