package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsHandler;

@Service
public class ApplicantChronologiesStatementHandler extends ChronologiesStatementsHandler {

    @Autowired
    public ApplicantChronologiesStatementHandler() {
        super(CaseDocumentCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION,
                CaseDocumentParty.APPLICANT);
    }
}
