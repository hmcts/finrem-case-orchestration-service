package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.ManageCaseDocumentsCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

@Service
public class RespondentExpertEvidenceHandler extends ExpertEvidenceHandler {

    @Autowired
    public RespondentExpertEvidenceHandler() {
        super(ManageCaseDocumentsCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION,
                CaseDocumentParty.RESPONDENT);
    }
}
