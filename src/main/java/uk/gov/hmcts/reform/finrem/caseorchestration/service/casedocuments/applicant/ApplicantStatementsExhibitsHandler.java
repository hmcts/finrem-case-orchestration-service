package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsHandler;

@Service
public class ApplicantStatementsExhibitsHandler extends StatementExhibitsHandler {

    @Autowired
    public ApplicantStatementsExhibitsHandler() {

        super(CaseDocumentCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
