package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

@Component
public class DraftOrdersCategoriser extends DocumentCategoriser {

    public DraftOrdersCategoriser(FeatureToggleService featureToggleService) {
        super(featureToggleService);
    }

    @Override
    protected void categoriseDocuments(FinremCaseData finremCaseData) {
        // Determine type of draft order
        if (!isSuggestedDraftOrderPriorToHearing(finremCaseData)) {
            return;
        }

        // Get the current user case role
        //NEED TO DO
        String chosenParty = determineChosenParty(finremCaseData);

        DocumentCategory category = determineCategory(chosenParty);
        if (category != null) {
            categoriseOrders(finremCaseData.getDraftOrdersWrapper()
                .getUploadSuggestedDraftOrder()
                .getSuggestedDraftOrderCollection(), category);

            categorisePSAs(finremCaseData.getDraftOrdersWrapper()
                .getUploadSuggestedDraftOrder()
                .getSuggestedPSACollection(), category);
        }

    }

    private boolean isSuggestedDraftOrderPriorToHearing(FinremCaseData finremCaseData) {
        return "aSuggestedDraftOrderPriorToAListedHearing".equals(finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder());
    }

    private String determineChosenParty(FinremCaseData finremCaseData) {
        return finremCaseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().getUploadParty();
    }

    private DocumentCategory determineCategory(String chosenParty) {
        switch (chosenParty) {
            case "theApplicant":
                return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER;
            case "theRespondent":
                return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER;
            default:
                return null;
        }
    }

    private void categoriseOrders(List<SuggestedDraftOrderCollection> orderCollections, DocumentCategory category) {
        Optional.ofNullable(orderCollections)
            .ifPresent(collections -> collections.forEach(collection -> setOrderCategoryIfAbsent(collection.getValue(), category)));
    }

    private void categorisePSAs(List<SuggestedPensionSharingAnnexCollection> psaCollections, DocumentCategory category) {
        Optional.ofNullable(psaCollections)
            .ifPresent(collections -> collections.forEach(collection -> setPSACategoryIfAbsent(collection.getValue(), category)));
    }

    private void setOrderCategoryIfAbsent(SuggestedDraftOrder order, DocumentCategory category) {
        if (order != null && order.getSuggestedDraftOrderDocument() != null
            && order.getSuggestedDraftOrderDocument().getCategoryId() == null) {
            order.getSuggestedDraftOrderDocument().setCategoryId(category.getDocumentCategoryId());
        }

        Optional.ofNullable(order)
            .map(SuggestedDraftOrder::getSuggestedDraftOrderAdditionalDocumentsCollection)
            .ifPresent(additionalDocs -> additionalDocs.forEach(doc -> setAdditionalDocumentsCategory(doc, category)));
    }

    private void setAdditionalDocumentsCategory(SuggestedDraftOrderAdditionalDocumentsCollection additionalDoc, DocumentCategory category) {
        if (additionalDoc != null && additionalDoc.getValue() != null
            && additionalDoc.getValue().getCategoryId() == null) {
            additionalDoc.getValue().setCategoryId(category.getDocumentCategoryId());
        }
    }

    private void setPSACategoryIfAbsent(SuggestedPensionSharingAnnex psa, DocumentCategory category) {
        if (psa != null && psa.getSuggestedPensionSharingAnnexes() != null
            && psa.getSuggestedPensionSharingAnnexes().getCategoryId() == null) {
            psa.getSuggestedPensionSharingAnnexes().setCategoryId(category.getDocumentCategoryId());
        }
    }
}