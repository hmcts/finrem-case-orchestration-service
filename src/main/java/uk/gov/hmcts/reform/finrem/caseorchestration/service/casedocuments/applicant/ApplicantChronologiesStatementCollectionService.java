package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ChronologiesStatementsCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

@Service
public class ApplicantChronologiesStatementCollectionService extends ChronologiesStatementsCollectionService {

    @Autowired
    public ApplicantChronologiesStatementCollectionService(
        EvidenceManagementDeleteService evidenceManagementDeleteService) {

        super(ManageCaseDocumentsCollectionType.APP_CHRONOLOGIES_STATEMENTS_COLLECTION,
            evidenceManagementDeleteService, CaseDocumentParty.APPLICANT);
    }
}
