package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsCollectionService;

@Service
public class ApplicantStatementsExhibitsCollectionService extends StatementExhibitsCollectionService {

    @Autowired
    public ApplicantStatementsExhibitsCollectionService() {

        super(ManageCaseDocumentsCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
