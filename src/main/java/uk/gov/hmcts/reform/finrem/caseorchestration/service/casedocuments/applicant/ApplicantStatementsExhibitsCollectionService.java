package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.StatementExhibitsCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

@Service
public class ApplicantStatementsExhibitsCollectionService extends StatementExhibitsCollectionService {

    @Autowired
    public ApplicantStatementsExhibitsCollectionService(
        EvidenceManagementDeleteService evidenceManagementDeleteService) {

        super(ManageCaseDocumentsCollectionType.APP_STATEMENTS_EXHIBITS_COLLECTION, evidenceManagementDeleteService,
            CaseDocumentParty.APPLICANT);
    }
}
