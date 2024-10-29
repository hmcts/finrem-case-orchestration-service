package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentParty.RESPONDENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.SUGGESTED_DRAFT_ORDER_OPTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

@Component
public class DraftOrdersCategoriser {

    public DraftOrdersCategoriser() {
        super();
    }

    public void categoriseDocuments(FinremCaseData finremCaseData, String userRole) {
        // Determine type of draft order
        if (!isSuggestedDraftOrderPriorToHearing(finremCaseData)) {
            return;
        }

        // Get the current user case role
        String chosenParty = determineChosenParty(finremCaseData, userRole);

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
        return SUGGESTED_DRAFT_ORDER_OPTION.equals(finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder());
    }

    private String determineChosenParty(FinremCaseData finremCaseData, String userRole) {
        if (finremCaseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().getUploadParty() != null) {
            String selectedPartyCode = finremCaseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().getUploadParty().getValue().getCode();

            if (UPLOAD_PARTY_APPLICANT.equals(selectedPartyCode)) {
                return APPLICANT.getValue();
            } else if (UPLOAD_PARTY_RESPONDENT.equals(selectedPartyCode)) {
                return RESPONDENT.getValue();
            }
            return selectedPartyCode;
        } else {
            return userRole;
        }
    }

    private DocumentCategory determineCategory(String chosenParty) {
        if (APPLICANT.getValue().equals(chosenParty)) {
            return DocumentCategory.HEARING_DOCUMENTS_APPLICANT_PRE_HEARING_DRAFT_ORDER;
        } else if (RESPONDENT.getValue().equals(chosenParty)) {
            return DocumentCategory.HEARING_DOCUMENTS_RESPONDENT_PRE_HEARING_DRAFT_ORDER;
        } else {
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
