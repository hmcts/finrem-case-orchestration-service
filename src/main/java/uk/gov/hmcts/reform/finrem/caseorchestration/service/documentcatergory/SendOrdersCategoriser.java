package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.stream.IntStream;

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
                    DocumentCategory.APPROVED_ORDERS.getDocumentCategoryId()));
        }

        categoryToAllPartiesOrders(finremCaseData.getOrderWrapper());

        categoryToAllPartiesOrders(finremCaseData.getConsentOrderWrapper());

    }

    private void categoryToAllPartiesOrders(OrderWrapper orderWrapper) {

        if (orderWrapper.getAppOrderCollections() != null) {
            orderWrapper.getAppOrderCollections().forEach(
                order -> IntStream.range(0, order.getValue().getApproveOrders().size()).forEach(idx -> {
                    final String categoryToApply = (idx <= 9) ? DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS
                        .getDocumentCategoryId() + (idx + 1) : DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS_OVERFLOW
                        .getDocumentCategoryId();
                    setCategoryToAllOrdersDocs(order.getValue().getApproveOrders().get(idx).getValue().getCaseDocument(),
                        categoryToApply);
                }));
        }
        if (orderWrapper.getRespOrderCollections() != null) {
            orderWrapper.getRespOrderCollections().forEach(
                order -> IntStream.range(0, order.getValue().getApproveOrders().size()).forEach(idx -> {
                    final String categoryToApply = (idx <= 9) ? DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS
                        .getDocumentCategoryId() + (idx + 1) : DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS_OVERFLOW
                        .getDocumentCategoryId();
                    setCategoryToAllOrdersDocs(order.getValue().getApproveOrders().get(idx).getValue().getCaseDocument(),
                        categoryToApply);
                }));
        }
        if (orderWrapper.getIntv1OrderCollections() != null) {
            orderWrapper.getIntv1OrderCollections().forEach(
                order -> IntStream.range(0, order.getValue().getApproveOrders().size()).forEach(idx -> {
                    final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS
                        .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS_OVERFLOW
                        .getDocumentCategoryId();
                    setCategoryToAllOrdersDocs(order.getValue().getApproveOrders().get(idx).getValue().getCaseDocument(),
                        categoryToApply);
                }));
        }
        if (orderWrapper.getIntv2OrderCollections() != null) {
            orderWrapper.getIntv2OrderCollections().forEach(
                order -> IntStream.range(0, order.getValue().getApproveOrders().size()).forEach(idx -> {
                    final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS
                        .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS_OVERFLOW
                        .getDocumentCategoryId();
                    setCategoryToAllOrdersDocs(order.getValue().getApproveOrders().get(idx).getValue().getCaseDocument(),
                        categoryToApply);
                }));
        }
        if (orderWrapper.getIntv3OrderCollections() != null) {
            orderWrapper.getIntv3OrderCollections().forEach(
                order -> IntStream.range(0, order.getValue().getApproveOrders().size()).forEach(idx -> {
                    final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS
                        .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS_OVERFLOW
                        .getDocumentCategoryId();
                    setCategoryToAllOrdersDocs(order.getValue().getApproveOrders().get(idx).getValue().getCaseDocument(),
                        categoryToApply);
                }));
        }
        if (orderWrapper.getIntv4OrderCollections() != null) {
            orderWrapper.getIntv4OrderCollections().forEach(
                order -> IntStream.range(0, order.getValue().getApproveOrders().size()).forEach(idx -> {
                    final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS
                        .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS_OVERFLOW
                        .getDocumentCategoryId();
                    setCategoryToAllOrdersDocs(order.getValue().getApproveOrders().get(idx).getValue().getCaseDocument(),
                        categoryToApply);
                }));
        }
    }

    private void categoryToAllPartiesOrders(ConsentOrderWrapper orderWrapper) {

        if (orderWrapper.getAppConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getAppConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = (idx <= 9) ? DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS
                    .getDocumentCategoryId() + (idx + 1) : DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS_OVERFLOW
                    .getDocumentCategoryId();
                setCategoryToAllOrdersDocs(orderWrapper.getAppConsentApprovedOrders().get(idx).getApprovedOrder().getConsentOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getRespConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getRespConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = (idx <= 9) ? DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS
                    .getDocumentCategoryId() + (idx + 1) : DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS_OVERFLOW
                    .getDocumentCategoryId();
                setCategoryToAllOrdersDocs(orderWrapper.getRespConsentApprovedOrders().get(idx).getApprovedOrder().getConsentOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv1ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv1ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS
                    .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS_OVERFLOW
                    .getDocumentCategoryId();
                setCategoryToAllOrdersDocs(orderWrapper.getIntv1ConsentApprovedOrders().get(idx).getApprovedOrder().getConsentOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv2ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv2ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS
                    .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS_OVERFLOW
                    .getDocumentCategoryId();
                setCategoryToAllOrdersDocs(orderWrapper.getIntv2ConsentApprovedOrders().get(idx).getApprovedOrder().getConsentOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv3ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv3ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS
                    .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS_OVERFLOW
                    .getDocumentCategoryId();
                setCategoryToAllOrdersDocs(orderWrapper.getIntv3ConsentApprovedOrders().get(idx).getApprovedOrder().getConsentOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv4ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv4ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = (idx <= 9) ? DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS
                    .getDocumentCategoryId() + (idx + 1) : DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS_OVERFLOW
                    .getDocumentCategoryId();
                setCategoryToAllOrdersDocs(orderWrapper.getIntv4ConsentApprovedOrders().get(idx).getApprovedOrder().getConsentOrder(),
                    categoryToApply);
            });
        }
    }

    private void setCategoryToAllOrdersDocs(CaseDocument document, String categoryToApply) {
        if (document != null) {
            document.setCategoryId(
                categoryToApply
            );
        }
    }
}
