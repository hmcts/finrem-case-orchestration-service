package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsCollectionService;

@Service
public class ApplicantChronologiesStatementCollectionService extends ChronologiesStatementsCollectionService {

    @Autowired
    public ApplicantChronologiesStatementCollectionService() {
        super(ManageCaseDocumentsCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION,
                CaseDocumentParty.APPLICANT);
    }
}
