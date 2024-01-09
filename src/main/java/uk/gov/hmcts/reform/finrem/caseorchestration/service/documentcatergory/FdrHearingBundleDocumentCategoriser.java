package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

@Component
@Slf4j
public class FdrHearingBundleDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public FdrHearingBundleDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        finremCaseData.getFdrHearingBundleCollections().forEach(fdrHearingBundleCollection -> {
            fdrHearingBundleCollection.getValue().getHearingBundleDocuments().forEach(hearingBundleDocument -> {
               hearingBundleDocument.getValue().getBundleDocuments().setCategoryId(DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE.getDocumentCategoryId());
            });
        });
    }
}
