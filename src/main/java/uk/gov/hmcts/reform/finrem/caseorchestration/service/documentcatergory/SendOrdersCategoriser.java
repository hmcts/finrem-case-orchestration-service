package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentInContestedApprovedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.PensionTypeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.stream.IntStream;

@Configuration
public class SendOrdersCategoriser extends DocumentCategoriser {

    private final DocumentHelper documentHelper;

    public SendOrdersCategoriser(FeatureToggleService featureToggleService, DocumentHelper documentHelper) {
        super(featureToggleService);
        this.documentHelper = documentHelper;
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {

        if (CollectionUtils.isNotEmpty(finremCaseData.getOrdersSentToPartiesCollection())) {
            List<OrderSentToPartiesCollection> partiesCollectionsCopy = documentHelper
                .deepCopyArray(finremCaseData.getOrdersSentToPartiesCollection(),
                    new TypeReference<List<OrderSentToPartiesCollection>>() {});
            partiesCollectionsCopy.forEach(
                order -> setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(),
                    DocumentCategory.ADMINISTRATIVE_DOCUMENTS_TRANSITIONAL.getDocumentCategoryId()));
            finremCaseData.setOrdersSentToPartiesCollection(partiesCollectionsCopy);
        }

        categoryToAllPartiesOrders(finremCaseData.getOrderWrapper());

        categoryToAllPartiesOrders(finremCaseData.getConsentOrderWrapper());

    }

    private void categoryToAllPartiesOrders(OrderWrapper orderWrapper) {

        if (orderWrapper.getAppOrderCollections() != null) {
            IntStream.range(0, orderWrapper.getAppOrderCollections().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS,
                    DocumentCategory.APPLICANT_DOCUMENTS_SEND_ORDERS_OVERFLOW);
                orderWrapper.getAppOrderCollections().get(idx).getValue().getApproveOrders().forEach(order ->
                    setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(), categoryToApply));
            });
        }
        if (orderWrapper.getRespOrderCollections() != null) {
            IntStream.range(0, orderWrapper.getRespOrderCollections().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS,
                    DocumentCategory.RESPONDENT_DOCUMENTS_SEND_ORDERS_OVERFLOW);
                orderWrapper.getRespOrderCollections().get(idx).getValue().getApproveOrders().forEach(order ->
                    setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(), categoryToApply));
            });
        }
        if (orderWrapper.getIntv1OrderCollections() != null) {
            IntStream.range(0, orderWrapper.getIntv1OrderCollections().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_SEND_ORDERS_OVERFLOW);
                orderWrapper.getIntv1OrderCollections().get(idx).getValue().getApproveOrders().forEach(order ->
                    setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(), categoryToApply));
            });
        }
        if (orderWrapper.getIntv2OrderCollections() != null) {
            IntStream.range(0, orderWrapper.getIntv2OrderCollections().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_SEND_ORDERS_OVERFLOW);
                orderWrapper.getIntv2OrderCollections().get(idx).getValue().getApproveOrders().forEach(order ->
                    setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(), categoryToApply));
            });
        }
        if (orderWrapper.getIntv3OrderCollections() != null) {
            IntStream.range(0, orderWrapper.getIntv3OrderCollections().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_SEND_ORDERS_OVERFLOW);
                orderWrapper.getIntv3OrderCollections().get(idx).getValue().getApproveOrders().forEach(order ->
                    setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(), categoryToApply));
            });
        }
        if (orderWrapper.getIntv4OrderCollections() != null) {
            IntStream.range(0, orderWrapper.getIntv4OrderCollections().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_SEND_ORDERS_OVERFLOW);
                orderWrapper.getIntv4OrderCollections().get(idx).getValue().getApproveOrders().forEach(order ->
                    setCategoryToAllOrdersDocs(order.getValue().getCaseDocument(), categoryToApply));
            });
        }
    }

    private void categoryToAllPartiesOrders(ConsentOrderWrapper orderWrapper) {

        if (orderWrapper.getAppConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getAppConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.APPLICANT_DOCUMENTS_CONSENT_ORDERS,
                    DocumentCategory.APPLICANT_DOCUMENTS_CONSENT_ORDERS_OVERFLOW);
                setCategoryToConsentInContestedOrdersDoc(orderWrapper.getAppConsentApprovedOrders().get(idx).getApprovedOrder(),
                    categoryToApply);

            });
        }
        if (orderWrapper.getRespConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getRespConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.RESPONDENT_DOCUMENTS_CONSENT_ORDERS,
                    DocumentCategory.RESPONDENT_DOCUMENTS_CONSENT_ORDERS_OVERFLOW);
                setCategoryToConsentInContestedOrdersDoc(orderWrapper.getRespConsentApprovedOrders().get(idx).getApprovedOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv1ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv1ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_CONSENT_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_1_CONSENT_ORDERS_OVERFLOW);
                setCategoryToConsentInContestedOrdersDoc(orderWrapper.getIntv1ConsentApprovedOrders().get(idx).getApprovedOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv2ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv2ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_CONSENT_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_2_CONSENT_ORDERS_OVERFLOW);
                setCategoryToConsentInContestedOrdersDoc(orderWrapper.getIntv2ConsentApprovedOrders().get(idx).getApprovedOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv3ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv3ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_CONSENT_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_3_CONSENT_ORDERS_OVERFLOW);
                setCategoryToConsentInContestedOrdersDoc(orderWrapper.getIntv3ConsentApprovedOrders().get(idx).getApprovedOrder(),
                    categoryToApply);
            });
        }
        if (orderWrapper.getIntv4ConsentApprovedOrders() != null) {
            IntStream.range(0, orderWrapper.getIntv4ConsentApprovedOrders().size()).forEach(idx -> {
                final String categoryToApply = getNumberCategory(idx,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_CONSENT_ORDERS,
                    DocumentCategory.INTERVENER_DOCUMENTS_INTERVENER_4_CONSENT_ORDERS_OVERFLOW);
                setCategoryToConsentInContestedOrdersDoc(orderWrapper.getIntv4ConsentApprovedOrders().get(idx).getApprovedOrder(),
                    categoryToApply);
            });
        }
    }

    private static String getNumberCategory(int idx, DocumentCategory numberCategory, DocumentCategory overflowCategory) {
        return (idx <= 9) ? numberCategory
            .getDocumentCategoryId() + (idx + 1) : overflowCategory
            .getDocumentCategoryId();
    }

    private void setCategoryToConsentInContestedOrdersDoc(ConsentInContestedApprovedOrder approvedOrder, String categoryToApply) {
        if (approvedOrder.getConsentOrder() != null) {
            CaseDocument consentOrderCopy = new CaseDocument(approvedOrder.getConsentOrder());
            consentOrderCopy.setCategoryId(categoryToApply);
            approvedOrder.setConsentOrder(consentOrderCopy);
        }

        if (approvedOrder.getOrderLetter() != null) {
            CaseDocument orderLetterCopy = new CaseDocument(approvedOrder.getOrderLetter());
            orderLetterCopy.setCategoryId(categoryToApply);
            approvedOrder.setOrderLetter(orderLetterCopy);
        }

        if (CollectionUtils.isNotEmpty(approvedOrder.getAdditionalConsentDocuments())) {
            List<DocumentCollection> additionalConsentDocumentsCopy = documentHelper.deepCopyArray(approvedOrder.getAdditionalConsentDocuments(),
                new TypeReference<List<DocumentCollection>>() {});
            additionalConsentDocumentsCopy.forEach(ad -> {
                setCategoryToAllOrdersDocs(ad.getValue(), categoryToApply);
            });
            approvedOrder.setAdditionalConsentDocuments(additionalConsentDocumentsCopy);
        }

        if (CollectionUtils.isNotEmpty(approvedOrder.getPensionDocuments())) {
            List<PensionTypeCollection> pensionDocumentsCopy = documentHelper.deepCopyArray(approvedOrder.getPensionDocuments(),
                new TypeReference<List<PensionTypeCollection>>() {});
            pensionDocumentsCopy.forEach(pd -> {
                setCategoryToAllOrdersDocs(pd.getTypedCaseDocument().getPensionDocument(), categoryToApply);
            });
            approvedOrder.setPensionDocuments(pensionDocumentsCopy);
        }

    }

    private void setCategoryToAllOrdersDocs(CaseDocument document, String categoryToApply) {
        if (document != null) {
            document.setCategoryId(categoryToApply);
        }
    }
}
