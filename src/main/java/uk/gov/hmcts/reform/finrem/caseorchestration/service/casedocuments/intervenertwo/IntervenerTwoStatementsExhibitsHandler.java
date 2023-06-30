package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenertwo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_TWO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType.INTV_TWO_STATEMENTS_EXHIBITS_COLLECTION;

@Component
public class IntervenerTwoStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public IntervenerTwoStatementsExhibitsHandler() {
        super(INTV_TWO_STATEMENTS_EXHIBITS_COLLECTION, INTERVENER_TWO);
    }
}
