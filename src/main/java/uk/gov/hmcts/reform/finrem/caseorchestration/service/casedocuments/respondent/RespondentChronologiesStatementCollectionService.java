package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsCollectionService;

@Service
public class RespondentChronologiesStatementCollectionService extends ChronologiesStatementsCollectionService {

    @Autowired
    public RespondentChronologiesStatementCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_CHRONOLOGIES_STATEMENTS_COLLECTION, CaseDocumentParty.RESPONDENT);
    }
}
