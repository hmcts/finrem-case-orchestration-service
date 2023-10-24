package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

public abstract class DocumentCategorizer {

    private final FeatureToggleService featureToggleService;

    public DocumentCategorizer(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    public void categorize(FinremCaseData finremCaseData) {
        if (featureToggleService.isCaseFileViewEnabled()) {
            categorizeDocuments(finremCaseData);
        }
    }

    protected abstract void categorizeDocuments(FinremCaseData finremCaseData);
}
