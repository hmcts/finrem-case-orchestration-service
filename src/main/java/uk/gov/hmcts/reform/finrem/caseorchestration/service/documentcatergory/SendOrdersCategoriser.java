package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
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

        categoryToAllPartiesOrders(finremCaseData.getOrderWrapper());

        categoryToAllPartiesOrders(finremCaseData.getConsentOrderWrapper());

    }

    private void categoryToAllPartiesOrders(OrderWrapper orderWrapper) {

        if (orderWrapper.getAppOrderCollections() != null) {
            orderWrapper.getAppOrderCollections().forEach(
                order -> order.getValue().getApproveOrders().forEach(col -> setCategoryToAllOrdersDocs(col.getValue().getCaseDocument(),
                    DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS)));
        }
        if (orderWrapper.getRespOrderCollections() != null) {
            orderWrapper.getRespOrderCollections().forEach(
                order -> order.getValue().getApproveOrders().forEach(col -> setCategoryToAllOrdersDocs(col.getValue().getCaseDocument(),
                    DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS)));
        }
        if (orderWrapper.getIntv1OrderCollections() != null) {
            orderWrapper.getIntv1OrderCollections().forEach(
                order -> order.getValue().getApproveOrders().forEach(col -> setCategoryToAllOrdersDocs(col.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS)));
        }
        if (orderWrapper.getIntv2OrderCollections() != null) {
            orderWrapper.getIntv2OrderCollections().forEach(
                order -> order.getValue().getApproveOrders().forEach(col -> setCategoryToAllOrdersDocs(col.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS)));
        }
        if (orderWrapper.getIntv3OrderCollections() != null) {
            orderWrapper.getIntv3OrderCollections().forEach(
                order -> order.getValue().getApproveOrders().forEach(col -> setCategoryToAllOrdersDocs(col.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS)));
        }
        if (orderWrapper.getIntv4OrderCollections() != null) {
            orderWrapper.getIntv4OrderCollections().forEach(
                order -> order.getValue().getApproveOrders().forEach(col -> setCategoryToAllOrdersDocs(col.getValue().getCaseDocument(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS)));
        }
    }

    private void categoryToAllPartiesOrders(ConsentOrderWrapper orderWrapper) {

        if (orderWrapper.getAppConsentApprovedOrders() != null) {
            orderWrapper.getAppConsentApprovedOrders().forEach(
                order -> setCategoryToAllOrdersDocs(order.getApprovedOrder().getConsentOrder(),
                    DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS));
        }
        if (orderWrapper.getRespConsentApprovedOrders() != null) {
            orderWrapper.getRespConsentApprovedOrders().forEach(
                order -> setCategoryToAllOrdersDocs(order.getApprovedOrder().getConsentOrder(),
                    DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS));
        }
        if (orderWrapper.getIntv1ConsentApprovedOrders() != null) {
            orderWrapper.getIntv1ConsentApprovedOrders().forEach(
                order -> setCategoryToAllOrdersDocs(order.getApprovedOrder().getConsentOrder(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS));
        }
        if (orderWrapper.getIntv2ConsentApprovedOrders() != null) {
            orderWrapper.getIntv2ConsentApprovedOrders().forEach(
                order -> setCategoryToAllOrdersDocs(order.getApprovedOrder().getConsentOrder(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS));
        }
        if (orderWrapper.getIntv3ConsentApprovedOrders() != null) {
            orderWrapper.getIntv3ConsentApprovedOrders().forEach(
                order -> setCategoryToAllOrdersDocs(order.getApprovedOrder().getConsentOrder(),
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS));
        }
        if (orderWrapper.getIntv4ConsentApprovedOrders() != null) {
            orderWrapper.getIntv4ConsentApprovedOrders().forEach(
                order -> setCategoryToAllOrdersDocs(order.getApprovedOrder().getConsentOrder(),
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
