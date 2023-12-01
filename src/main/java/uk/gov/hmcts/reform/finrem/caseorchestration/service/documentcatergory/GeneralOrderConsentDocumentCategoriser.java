package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;

@Component
@Slf4j
public class GeneralOrderConsentDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public GeneralOrderConsentDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        log.info("Categorising general order consent documents for case with Case ID: {}", finremCaseData.getCcdCaseId());
        List<ContestedGeneralOrderCollection> generalOrders =
            finremCaseData.getGeneralOrderWrapper().getGeneralOrdersConsent();
        if (generalOrders != null && !generalOrders.isEmpty()) {
            for (ContestedGeneralOrderCollection generalOrder : generalOrders) {
                CaseDocument generalOrderDocument = generalOrder.getValue().getAdditionalDocument();
                if (generalOrderDocument != null && generalOrderDocument.getCategoryId() == null) {
                    generalOrderDocument.setCategoryId(
                        DocumentCategory.APPROVED_ORDERS_CONSENT_APPLICATION.getDocumentCategoryId());
                }
            }
        }
    }
}
