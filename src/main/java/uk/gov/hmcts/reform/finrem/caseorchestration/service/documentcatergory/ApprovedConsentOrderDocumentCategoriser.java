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
public class ApprovedConsentOrderDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public ApprovedConsentOrderDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        log.info("Categorising approved consent order documents for case with Case ID: {}", finremCaseData.getCcdCaseId());
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
