package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

public abstract class DocumentCategoriser {

    private final FeatureToggleService featureToggleService;

    public DocumentCategoriser(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }


    public void categorise(FinremCaseData finremCaseData) {
        if (featureToggleService.isCaseFileViewEnabled()) {
            categoriseDocuments(finremCaseData);
        }
    }

    protected abstract void categoriseDocuments(FinremCaseData finremCaseData);
}
