package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.OrderFiledBy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;

@Component
public class DraftOrdersCategoriser {

    public DraftOrdersCategoriser() {
        super();
    }

    /**
     * Categorise the uploaded suggested draft order documents based on the order filed by value.
     * The category is set only if the type of draft order upload is 'Suggested Draft Order'.
     *
     * @param finremCaseData the case data
     */
    public void categoriseDocuments(FinremCaseData finremCaseData) {
        // Determine type of draft order
        if (!isSuggestedDraftOrderPriorToHearing(finremCaseData)) {
            return;
        }

        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = finremCaseData.getDraftOrdersWrapper()
            .getUploadSuggestedDraftOrder();
        OrderFiledBy orderParty = uploadSuggestedDraftOrder.getOrderFiledBy();

        DocumentCategory category = getDocumentCategory(orderParty);

        categoriseOrders(uploadSuggestedDraftOrder.getUploadSuggestedDraftOrderCollection(), category);
        categorisePsas(uploadSuggestedDraftOrder.getSuggestedPsaCollection(), category);
    }

    private boolean isSuggestedDraftOrderPriorToHearing(FinremCaseData finremCaseData) {
        return SUGGESTED_DRAFT_ORDER_OPTION.equals(finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder());
    }

    private DocumentCategory getDocumentCategory(OrderFiledBy orderFiledBy) {
        return switch (orderFiledBy) {
            case APPLICANT, APPLICANT_BARRISTER -> DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER;
            case RESPONDENT, RESPONDENT_BARRISTER -> DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER;
            case INTERVENER_1 -> DocumentCategory.HEARING_DOCUMENTS_INTERVENER_1_PRE_HEARING_DRAFT_ORDER;
            case INTERVENER_2 -> DocumentCategory.HEARING_DOCUMENTS_INTERVENER_2_PRE_HEARING_DRAFT_ORDER;
            case INTERVENER_3 -> DocumentCategory.HEARING_DOCUMENTS_INTERVENER_3_PRE_HEARING_DRAFT_ORDER;
            case INTERVENER_4 -> DocumentCategory.HEARING_DOCUMENTS_INTERVENER_4_PRE_HEARING_DRAFT_ORDER;
        };
    }

    private void categoriseOrders(List<UploadSuggestedDraftOrderCollection> orderCollections, DocumentCategory category) {
        Optional.ofNullable(orderCollections)
            .ifPresent(collections -> collections.forEach(collection -> setOrderCategoryIfAbsent(collection.getValue(), category)));
    }

    private void categorisePsas(List<SuggestedPensionSharingAnnexCollection> psaCollections, DocumentCategory category) {
        Optional.ofNullable(psaCollections)
            .ifPresent(collections -> collections.forEach(collection -> setPsaCategoryIfAbsent(collection.getValue(), category)));
    }

    private void setOrderCategoryIfAbsent(UploadedDraftOrder order, DocumentCategory category) {
        if (order != null && order.getSuggestedDraftOrderDocument() != null
            && order.getSuggestedDraftOrderDocument().getCategoryId() == null) {
            order.getSuggestedDraftOrderDocument().setCategoryId(category.getDocumentCategoryId());
        }

        Optional.ofNullable(order)
            .map(UploadedDraftOrder::getSuggestedDraftOrderAdditionalDocumentsCollection)
            .ifPresent(additionalDocs -> additionalDocs.forEach(doc -> setAdditionalDocumentsCategory(doc, category)));
    }

    private void setAdditionalDocumentsCategory(SuggestedDraftOrderAdditionalDocumentsCollection additionalDoc, DocumentCategory category) {
        if (additionalDoc != null && additionalDoc.getValue() != null
            && additionalDoc.getValue().getCategoryId() == null) {
            additionalDoc.getValue().setCategoryId(category.getDocumentCategoryId());
        }
    }

    private void setPsaCategoryIfAbsent(SuggestedPensionSharingAnnex psa, DocumentCategory category) {
        if (psa != null && psa.getSuggestedPensionSharingAnnexes() != null
            && psa.getSuggestedPensionSharingAnnexes().getCategoryId() == null) {
            psa.getSuggestedPensionSharingAnnexes().setCategoryId(category.getDocumentCategoryId());
        }
    }
}
