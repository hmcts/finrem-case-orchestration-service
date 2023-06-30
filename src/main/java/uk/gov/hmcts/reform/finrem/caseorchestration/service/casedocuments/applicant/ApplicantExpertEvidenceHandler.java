package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.applicant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

@Service
public class ApplicantExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public ApplicantExpertEvidenceHandler() {
        super(ManageCaseDocumentsCollectionType.APP_EXPERT_EVIDENCE_COLLECTION,
            CaseDocumentParty.APPLICANT);
    }
}
