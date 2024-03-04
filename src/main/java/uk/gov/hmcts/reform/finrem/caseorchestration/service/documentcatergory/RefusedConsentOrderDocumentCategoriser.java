package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
@Slf4j
public class RefusedConsentOrderDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public RefusedConsentOrderDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        log.info("Categorising refused consent order documents for case with Case ID: {}", finremCaseData.getCcdCaseId());
        List<ConsentOrderCollection> refusedOrders =
            finremCaseData.getConsentOrderWrapper().getConsentedNotApprovedOrders();
        if (refusedOrders != null && !refusedOrders.isEmpty()) {
            for (ConsentOrderCollection refusedOrder : refusedOrders) {
                CaseDocument refusedConsentOrder = refusedOrder.getApprovedOrder().getConsentOrder();
                if (refusedConsentOrder != null && refusedConsentOrder.getCategoryId() == null) {
                    refusedConsentOrder.setCategoryId(
                        DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS.getDocumentCategoryId());
                }
            }
        }
    }
}
