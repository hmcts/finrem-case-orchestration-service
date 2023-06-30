package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

@Service
public class RespondentChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public RespondentChronologiesStatementHandler() {
        super(ManageCaseDocumentsCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION,
            CaseDocumentParty.RESPONDENT);
    }
}
