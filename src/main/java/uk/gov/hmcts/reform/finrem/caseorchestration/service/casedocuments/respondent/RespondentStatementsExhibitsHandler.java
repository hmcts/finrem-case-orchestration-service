package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

@Service
public class RespondentStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public RespondentStatementsExhibitsHandler() {
        super(ManageCaseDocumentsCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
