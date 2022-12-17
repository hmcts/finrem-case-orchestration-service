package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsCollectionService;

@Service
public class RespondentStatementsExhibitsCollectionService extends StatementExhibitsCollectionService {

    @Autowired
    public RespondentStatementsExhibitsCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_STATEMENTS_EXHIBITS_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
