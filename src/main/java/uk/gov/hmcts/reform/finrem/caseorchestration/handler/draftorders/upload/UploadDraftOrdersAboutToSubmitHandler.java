package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.CaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.suggested.SuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedDraftOrderAdditionalDocumentsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnex;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.SuggestedPensionSharingAnnexCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.DraftOrdersCategoriser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UploadDraftOrdersAboutToSubmitHandler extends FinremCallbackHandler {

    private final DraftOrdersCategoriser draftOrdersCategoriser;


    public UploadDraftOrdersAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, DraftOrdersCategoriser draftOrdersCategoriser) {
        super(finremCaseDetailsMapper);
        this.draftOrdersCategoriser = draftOrdersCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to start callback for Case ID: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();

        if ("aSuggestedDraftOrderPriorToAListedHearing".equals(finremCaseData.getDraftOrdersWrapper().getTypeOfDraftOrder())) {
            handleSuggestedDraftOrders(finremCaseData);
        } else {
            handleAgreedDraftOrder(finremCaseData);
        }

        draftOrdersCategoriser.categorise(finremCaseData);


        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private void handleAgreedDraftOrder(FinremCaseData finremCaseData) {
        // TODO for agreed
    }

    private void handleSuggestedDraftOrders(FinremCaseData finremCaseData) {
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = draftOrdersWrapper.getUploadSuggestedDraftOrder();

        if (uploadSuggestedDraftOrder != null) {
            List<SuggestedDraftOrderCollection> newSuggestedDraftOrderCollections = processSuggestedDraftOrders(uploadSuggestedDraftOrder);

            List<SuggestedDraftOrderCollection> existingSuggestedDraftOrderCollections =
                getExistingSuggestedDraftOrderCollections(draftOrdersWrapper);

            existingSuggestedDraftOrderCollections.addAll(newSuggestedDraftOrderCollections);
            draftOrdersWrapper.setSuggestedDraftOrderCollection(existingSuggestedDraftOrderCollections);

            draftOrdersWrapper.setUploadSuggestedDraftOrder(null); // Clear the temporary field
        }
    }

    private List<SuggestedDraftOrderCollection> processSuggestedDraftOrders(UploadSuggestedDraftOrder uploadSuggestedDraftOrder) {
        List<SuggestedDraftOrderCollection> newSuggestedDraftOrderCollections = new ArrayList<>();
        List<String> uploadOrdersOrPsas = uploadSuggestedDraftOrder.getUploadOrdersOrPsas();

        // First check if 'order' is selected
        if (uploadOrdersOrPsas.contains("orders")) {
            for (UploadSuggestedDraftOrderCollection uploadCollection : uploadSuggestedDraftOrder.getUploadSuggestedDraftOrderCollection()) {
                UploadedDraftOrder uploadDraftOrder = uploadCollection.getValue();

                // Create the draft order element
                SuggestedDraftOrder orderDraftOrder = mapToSuggestedDraftOrder(uploadDraftOrder, uploadSuggestedDraftOrder);

                SuggestedDraftOrderCollection orderCollection =
                    SuggestedDraftOrderCollection.builder()
                        .value(orderDraftOrder)
                        .build();

                newSuggestedDraftOrderCollections.add(orderCollection);
            }
        }

        //check if 'psa' is selected
        if (uploadOrdersOrPsas.contains("pensionSharingAnnexes")) {
            for (SuggestedPensionSharingAnnexCollection psaCollection : uploadSuggestedDraftOrder.getSuggestedPsaCollection()) {
                SuggestedPensionSharingAnnex uploadPsa = psaCollection.getValue();

                //create the PSA element
                SuggestedDraftOrder psaDraftOrder = mapToSuggestedDraftOrderForPsa(uploadPsa, uploadSuggestedDraftOrder);

                SuggestedDraftOrderCollection psaOrderCollection =
                    SuggestedDraftOrderCollection.builder()
                        .value(psaDraftOrder)
                        .build();

                newSuggestedDraftOrderCollections.add(psaOrderCollection);
            }
        }

        return newSuggestedDraftOrderCollections;
    }

    private SuggestedDraftOrder mapToSuggestedDraftOrder(
        UploadedDraftOrder uploadDraftOrder,
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder) {

        SuggestedDraftOrder.SuggestedDraftOrderBuilder suggestedDraftOrderBuilder = SuggestedDraftOrder.builder()
            .submittedBy("FIX this, we need to show caseworker") // Adjust as needed
            .uploadedOnBehalfOf(uploadSuggestedDraftOrder.getUploadParty().getValue().getCode())
            .submittedDate(LocalDateTime.now());

        //Map the draft order document
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getSuggestedDraftOrderDocument()) {
            suggestedDraftOrderBuilder.draftOrder(uploadDraftOrder.getSuggestedDraftOrderDocument());
        }

        // Add additional attachments for orders only
        if (!ObjectUtils.isEmpty(uploadDraftOrder.getSuggestedDraftOrderAdditionalDocumentsCollection())) {
            List<CaseDocumentCollection> attachments = new ArrayList<>();
            for (SuggestedDraftOrderAdditionalDocumentsCollection additionalDoc :
                uploadDraftOrder.getSuggestedDraftOrderAdditionalDocumentsCollection()) {
                if (additionalDoc.getValue() != null) {
                    attachments.add(CaseDocumentCollection.builder()
                        .value(additionalDoc.getValue())
                        .build());
                }
            }
            suggestedDraftOrderBuilder.attachments(attachments);
        }

        return suggestedDraftOrderBuilder.build();
    }

    private SuggestedDraftOrder mapToSuggestedDraftOrderForPsa(
        SuggestedPensionSharingAnnex uploadPsa,
        UploadSuggestedDraftOrder uploadSuggestedDraftOrder) {

        SuggestedDraftOrder.SuggestedDraftOrderBuilder suggestedDraftOrderBuilder = SuggestedDraftOrder.builder()
            .submittedBy("FIX this, we need to show caseworker")
            .uploadedOnBehalfOf(uploadSuggestedDraftOrder.getUploadParty().getValue().getCode())
            .submittedDate(LocalDateTime.now());

        // Map the PSA document
        if (uploadPsa != null) {
            suggestedDraftOrderBuilder.pensionSharingAnnex(uploadPsa.getSuggestedPensionSharingAnnexes());
        }

        return suggestedDraftOrderBuilder.build();
    }

    private List<SuggestedDraftOrderCollection> getExistingSuggestedDraftOrderCollections(DraftOrdersWrapper draftOrdersWrapper) {
        List<SuggestedDraftOrderCollection> existingSuggestedDraftOrderCollections = draftOrdersWrapper.getSuggestedDraftOrderCollection();
        if (existingSuggestedDraftOrderCollections == null) {
            existingSuggestedDraftOrderCollections = new ArrayList<>();
        }
        return existingSuggestedDraftOrderCollections;
    }
}
