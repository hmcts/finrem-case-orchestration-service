package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_ONE_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerOneStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public IntervenerOneStatementsExhibitsHandler() {
        super(INTV_ONE_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_ONE);
    }
}
