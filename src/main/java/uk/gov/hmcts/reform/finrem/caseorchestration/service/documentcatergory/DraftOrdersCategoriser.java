package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.UPLOAD_PARTY_RESPONDENT;

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
                .getUploadSuggestedDraftOrderCollection(), category);

            categorisePsas(finremCaseData.getDraftOrdersWrapper()
                .getUploadSuggestedDraftOrder()
                .getSuggestedPsaCollection(), category);
        }

    }

    private boolean isSuggestedDraftOrderPriorToHearing(FinremCaseData finremCaseData) {
        return "aSuggestedDraftOrderPriorToAListedHearing".equals(finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder());
    }

    private String determineChosenParty(FinremCaseData finremCaseData) {

        String selectedPartyCode = finremCaseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().getUploadParty().getValue().getCode();

        if (UPLOAD_PARTY_APPLICANT.equals(selectedPartyCode)) {
            return UPLOAD_PARTY_APPLICANT;
        } else if (UPLOAD_PARTY_RESPONDENT.equals(selectedPartyCode)) {
            return UPLOAD_PARTY_RESPONDENT;
        }
        return selectedPartyCode;
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
