package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DraftDirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

@Component
public class UploadedDraftOrderCategoriser extends DocumentCategoriser {

    public UploadedDraftOrderCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        categoriseOrders(finremCaseData.getDraftDirectionWrapper().getJudgesAmendedOrderCollection(), DocumentCategory.SYSTEM_DUPLICATES);
        categoriseOrders(finremCaseData.getDraftDirectionWrapper().getDraftDirectionOrderCollection(), DocumentCategory.POST_HEARING_DRAFT_ORDER);
    }

    private void categoriseOrders(List<DraftDirectionOrderCollection> orderCollections, DocumentCategory category) {
        Optional.ofNullable(orderCollections)
            .ifPresent(collections -> collections.forEach(collection -> setCategoryIfAbsent(collection.getValue(), category)));
    }

    private void setCategoryIfAbsent(DraftDirectionOrder order, DocumentCategory category) {
        if (order != null && order.getUploadDraftDocument() != null && order.getUploadDraftDocument().getCategoryId() == null) {
            order.getUploadDraftDocument().setCategoryId(category.getDocumentCategoryId());
        }
    }
}

