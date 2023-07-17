package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerfour;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_FOUR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerFourStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public IntervenerFourStatementsExhibitsHandler() {
        super(INTERVENER_FOUR_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_FOUR);
    }
}
