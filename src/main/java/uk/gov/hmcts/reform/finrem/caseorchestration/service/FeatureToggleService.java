package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;

public interface FeatureToggleService {
    boolean isFeatureEnabled(Features feature);
}
