package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.CaseSummariesCollectionService;

@Service
public class ApplicantCaseSummariesCollectionService extends CaseSummariesCollectionService {

    @Autowired
    public ApplicantCaseSummariesCollectionService() {
        super(ManageCaseDocumentsCollectionType.APP_CASE_SUMMARIES_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
