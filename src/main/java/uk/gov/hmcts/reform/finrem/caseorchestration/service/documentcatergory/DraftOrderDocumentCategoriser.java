package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
@Slf4j
public class DraftOrderDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public DraftOrderDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        categoriseHearingOrders(finremCaseData);
        categoriseFinalOrders(finremCaseData);
    }

    private static void categoriseHearingOrders(FinremCaseData finremCaseData) {
        log.info("Categorising hearing order documents into hearing notices for case with Case ID: {}", finremCaseData.getCcdCaseId());
        List<DirectionOrderCollection> hearingOrders =
            finremCaseData.getUploadHearingOrder();
        setCategory(hearingOrders, DocumentCategory.HEARING_NOTICES.getDocumentCategoryId());
    }

    private static void categoriseFinalOrders(FinremCaseData finremCaseData) {
        log.info("Categorising final order documents into approved orders for case with Case ID: {}", finremCaseData.getCcdCaseId());
        List<DirectionOrderCollection> finalOrders =
            finremCaseData.getFinalOrderCollection();
        setCategory(finalOrders, DocumentCategory.APPROVED_ORDERS_CASE.getDocumentCategoryId());
    }

    private static void setCategory(List<DirectionOrderCollection> orders, String categoryId) {
        if (orders != null && !orders.isEmpty()) {
            for (DirectionOrderCollection order : orders) {
                CaseDocument draftDocument = order.getValue().getUploadDraftDocument();
                if (draftDocument != null && draftDocument.getCategoryId() == null) {
                    draftDocument.setCategoryId(
                        categoryId);
                }
            }
        }
    }
}
