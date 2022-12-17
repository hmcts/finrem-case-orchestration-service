package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceCollectionService;

@Service
public class RespondentExpertEvidenceCollectionService extends ExpertEvidenceCollectionService {

    @Autowired
    public RespondentExpertEvidenceCollectionService() {
        super(ManageCaseDocumentsCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
