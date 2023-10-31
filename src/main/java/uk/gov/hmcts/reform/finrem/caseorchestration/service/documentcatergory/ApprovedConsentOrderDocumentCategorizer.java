package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
public class ApprovedConsentOrderDocumentCategorizer extends DocumentCategorizer {

    @Autowired
    public ApprovedConsentOrderDocumentCategorizer(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categorizeDocuments(FinremCaseData finremCaseData) {
        List<ConsentOrderCollection> approvedOrders =
            finremCaseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders();
        if (approvedOrders != null && !approvedOrders.isEmpty()) {
            for (ConsentOrderCollection approvedOrder : approvedOrders) {
                CaseDocument orderLetter = approvedOrder.getApprovedOrder().getOrderLetter();
                if (orderLetter != null && orderLetter.getCategoryId() == null) {
                    orderLetter.setCategoryId(
                        DocumentCategory.APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId());
                }
            }
        }
    }
}
