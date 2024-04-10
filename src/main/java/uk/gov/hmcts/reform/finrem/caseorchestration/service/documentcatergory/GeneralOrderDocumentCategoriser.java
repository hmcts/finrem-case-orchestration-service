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
public class GeneralOrderDocumentCategoriser extends DocumentCategoriser {

    @Autowired
    public GeneralOrderDocumentCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        List<ContestedGeneralOrderCollection> generalOrders =
            finremCaseData.getGeneralOrderWrapper().getGeneralOrders();
        log.info("Categorising general order documents for case with Case ID: {}", finremCaseData.getCcdCaseId());
        categoriseGeneralOrders(generalOrders, DocumentCategory.APPROVED_ORDERS_CASE);

        List<ContestedGeneralOrderCollection> generalOrdersConsent =
            finremCaseData.getGeneralOrderWrapper().getGeneralOrdersConsent();
        log.info("Categorising general order consent documents for case with Case ID: {}", finremCaseData.getCcdCaseId());
        categoriseGeneralOrders(generalOrdersConsent, DocumentCategory.APPROVED_ORDERS_CONSENT_ORDER_TO_FINALISE_PROCEEDINGS);
    }

    private static void categoriseGeneralOrders(List<ContestedGeneralOrderCollection> generalOrderList,
                                                DocumentCategory documentCategory) {
        if (generalOrderList != null && !generalOrderList.isEmpty()) {
            for (ContestedGeneralOrderCollection generalOrder : generalOrderList) {
                CaseDocument generalOrderDocument = generalOrder.getValue().getAdditionalDocument();
                if (generalOrderDocument != null && generalOrderDocument.getCategoryId() == null) {
                    generalOrderDocument.setCategoryId(
                        documentCategory.getDocumentCategoryId());
                }
            }
        }
    }
}
