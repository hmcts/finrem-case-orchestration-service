package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

@Configuration
public class SendOrdersCategoriser extends DocumentCategoriser {


    public SendOrdersCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {

        if (CollectionUtils.isNotEmpty(finremCaseData.getOrdersSentToPartiesCollection())) {
            finremCaseData.getOrdersSentToPartiesCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.APPROVED_ORDERS));
        }

        OrderWrapper orderWrapper = finremCaseData.getOrderWrapper();
        if (orderWrapper.getAppOrderCollection() != null) {
            orderWrapper.getAppOrderCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS));
        }
        if (orderWrapper.getRespOrderCollection() != null) {
            orderWrapper.getRespOrderCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS));
        }
        if (orderWrapper.getIntv1OrderCollection() != null) {
            orderWrapper.getIntv1OrderCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS));
        }
        if (orderWrapper.getIntv2OrderCollection() != null) {
            orderWrapper.getIntv2OrderCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS));
        }
        if (orderWrapper.getIntv3OrderCollection() != null) {
            orderWrapper.getIntv3OrderCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS));
        }
        if (orderWrapper.getIntv4OrderCollection() != null) {
            orderWrapper.getIntv4OrderCollection().forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS));
        }
    }

    private void setCategoryToAllOrdersDocs(CaseDocument document, DocumentCategory categoryToApply) {
        if (document != null) {
            document.setCategoryId(
                categoryToApply.getDocumentCategoryId()
            );
        }
    }
}
