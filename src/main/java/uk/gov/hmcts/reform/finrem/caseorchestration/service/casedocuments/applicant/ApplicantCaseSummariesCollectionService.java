package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesCollectionService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;

@Service
public class ApplicantCaseSummariesCollectionService extends CaseSummariesCollectionService {

    @Autowired
    public ApplicantCaseSummariesCollectionService(EvidenceManagementDeleteService evidenceManagementDeleteService) {
        super(ManageCaseDocumentsCollectionType.APP_CASE_SUMMARIES_COLLECTION,
            evidenceManagementDeleteService,
            CaseDocumentParty.APPLICANT);
    }
}
