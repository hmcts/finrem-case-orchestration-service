package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DocumentCategoriser;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@Slf4j
public class HearingBundleDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public HearingBundleDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        if (finremCaseData.getFdrHearingBundleCollections() != null) {
            applyBundleCategory(finremCaseData.getFdrHearingBundleCollections(), DocumentCategory.FDR_BUNDLE);
        }
        if (finremCaseData.getHearingUploadBundle() != null) {
            applyBundleCategory(finremCaseData.getHearingUploadBundle(), DocumentCategory.HEARING_BUNDLE);
        }
    }

    private static void applyBundleCategory(List<HearingUploadBundleCollection> finremCaseData, DocumentCategory hearingBundle) {
        finremCaseData.forEach(hearingUploadBundle ->
            hearingUploadBundle.getValue().getHearingBundleDocuments().forEach(hearingBundleDocument -> {
                ofNullable(hearingBundleDocument.getValue().getBundleDocuments()).ifPresent(d ->
                    d.setCategoryId(hearingBundle.getDocumentCategoryId()));
            })
        );
    }
}
