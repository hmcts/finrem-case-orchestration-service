package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.respondent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

@Service
public class RespondentExpertEvidenceHandler extends ExpertEvidenceHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public RespondentExpertEvidenceHandler(FeatureToggleService featureToggleService) {
        super(CaseDocumentCollectionType.RESP_EXPERT_EVIDENCE_COLLECTION,
                CaseDocumentParty.RESPONDENT, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
