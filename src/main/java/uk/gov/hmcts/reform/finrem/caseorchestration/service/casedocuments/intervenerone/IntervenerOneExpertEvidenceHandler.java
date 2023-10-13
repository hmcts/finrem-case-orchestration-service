package uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.intervenerone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.casedocuments.ExpertEvidenceHandler;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.INTERVENER_ONE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CaseDocumentCollectionType.INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION;

@Component
public class IntervenerOneExpertEvidenceHandler extends ExpertEvidenceHandler {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public IntervenerOneExpertEvidenceHandler(FeatureToggleService featureToggleService) {
        super(INTERVENER_ONE_EXPERT_EVIDENCE_COLLECTION, INTERVENER_ONE, featureToggleService);
        this.featureToggleService = featureToggleService;
    }
}
