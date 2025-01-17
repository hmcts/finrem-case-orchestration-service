package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ContestedGeneralOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.FinalisedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.agreed.AgreedDraftOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrderDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.DraftOrdersReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocReviewCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.review.PsaDocumentReview;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderDateService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderPartyDocumentHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.ListUtils.defaultIfNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Slf4j
@Service
public class SendOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList;
    private final OrderDateService dateService;
    private final SendOrdersCategoriser sendOrdersCategoriser;

    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService,
                                                  GenericDocumentService genericDocumentService,
                                                  DocumentHelper documentHelper,
                                                  List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList,
                                                  OrderDateService dateService,
                                                  SendOrdersCategoriser sendOrdersCategoriser) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.sendOrderPartyDocumentList = sendOrderPartyDocumentList;
        this.dateService =  dateService;
        this.sendOrdersCategoriser = sendOrdersCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {}, callback {} callback for Case ID: {}",
            callbackRequest.getEventType(), CallbackType.ABOUT_TO_SUBMIT, caseId);

        try {
            FinremCaseData caseData = caseDetails.getData();
            List<String> parties = generalOrderService.getParties(caseDetails);
            log.info("Selected parties {} on Case ID: {}", parties, caseId);

            DynamicMultiSelectList selectedOrders = caseData.getOrdersToShare();
            log.info("Selected orders {} on Case ID: {} ", selectedOrders, caseId);

            List<OrderSentToPartiesCollection> printOrderCollection = new ArrayList<>();
            CaseDocument document = caseData.getAdditionalDocument();
            if (document != null) {
                log.info("Additional uploaded document with send order {} for Case ID: {}", document, caseId);
                CaseDocument additionalUploadedOrderDoc = genericDocumentService.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId);
                printOrderCollection.add(addToPrintOrderCollection(additionalUploadedOrderDoc));
                caseData.setAdditionalDocument(additionalUploadedOrderDoc);
            }

            log.info("Share and print general with for Case ID: {}", caseDetails.getId());
            shareAndSendGeneralOrderWithSelectedParties(caseDetails, parties, selectedOrders, printOrderCollection);

            log.info("Share and print hearing order for Case ID: {}", caseDetails.getId());
            Pair<List<CaseDocument>, List<CaseDocument>> hearingOrders = generalOrderService.hearingOrdersToShare(caseDetails, selectedOrders);
            List<CaseDocument> legacyHearingOrders = hearingOrders.getLeft();
            List<CaseDocument> newProcessedOrders = hearingOrders.getRight();

            if (hasApprovedOrdersToBeSent(legacyHearingOrders, newProcessedOrders)) {
                // below method also adds the cover sheet even if legacyHearingOrders is empty.
                shareAndSendHearingDocuments(caseDetails, legacyHearingOrders, parties, printOrderCollection, userAuthorisation);
                log.info("Sending for stamp final order on Case ID: {}", caseDetails.getId());
                // stamping legacy approved orders and add it to legacy finalised collection
                legacyHearingOrders.forEach(orderToStamp -> {
                    log.info("StampFinalOrder {} for Case ID: {}, ", orderToStamp, caseId);
                    stampAndAddToCollection(caseDetails, orderToStamp, userAuthorisation);
                });

                // handling processed orders
                moveApprovedDocumentsToFinalisedOrder(caseData, newProcessedOrders);
            }
            caseData.setOrdersSentToPartiesCollection(printOrderCollection);
            setConsolidateView(caseDetails, parties);

            clearTemporaryFields(caseData);
            sendOrdersCategoriser.categorise(caseDetails.getData());
        } catch (RuntimeException e) {
            log.error(format("%s on Case ID: %s", e.getMessage(), caseDetails.getId()), e);
            // The purpose of this catch block is to make the exception message available in the error message box
            // And it doesn't let CCD to retry if we populate the exception message to `errors`
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

    private boolean hasApprovedOrdersToBeSent(List<CaseDocument> legacyHearingOrders, List<CaseDocument> newProcessedOrders) {
        return (legacyHearingOrders != null && !legacyHearingOrders.isEmpty()) || (newProcessedOrders != null && !newProcessedOrders.isEmpty());
    }

    private Pair<List<PsaDocumentReview>, List<DraftOrderDocumentReview>> removeDocumentFromDraftOrderReview(FinremCaseData caseData,
                                                                                                              List<CaseDocument> hearingOrders) {

        List<PsaDocumentReview> removedPsaDocuments = new ArrayList<>();
        List<DraftOrderDocumentReview> removedDraftOrderDocuments = new ArrayList<>();

        emptyIfNull(caseData.getDraftOrdersWrapper().getDraftOrdersReviewCollection()).forEach(draftOrdersReviewCollection -> {
            DraftOrdersReview draftReview = draftOrdersReviewCollection.getValue();
            if (draftReview == null) {
                return;
            }

            hearingOrders.forEach(targetDocument -> {
                removeMatchingDocuments(draftReview.getPsaDocReviewCollection(),
                    PsaDocReviewCollection::getValue,
                    PsaDocumentReview::getPsaDocument,
                    targetDocument,
                    removedPsaDocuments);

                removeMatchingDocuments(draftReview.getDraftOrderDocReviewCollection(),
                    DraftOrderDocReviewCollection::getValue,
                    DraftOrderDocumentReview::getDraftOrderDocument,
                    targetDocument,
                    removedDraftOrderDocuments);
            });
        });

        return Pair.of(removedPsaDocuments, removedDraftOrderDocuments);
    }

    private <T, C> void removeMatchingDocuments(List<C> collection, Function<C, T> valueExtractor, Function<T, CaseDocument> docExtractor,
                                                CaseDocument targetDocument, List<T> removedItems) {
        if (collection == null) {
            return;
        }
        List<C> toRemove = collection.stream()
            .filter(item -> Optional.ofNullable(valueExtractor.apply(item))
                .map(docExtractor)
                .filter(doc -> doc.equals(targetDocument))
                .isPresent())
            .toList();

        toRemove.forEach(item -> removedItems.add(valueExtractor.apply(item)));
        collection.removeAll(toRemove);
    }

    private void removeDocumentFromsAgreedDraftOrderCollection(FinremCaseData caseData, List<CaseDocument> hearingOrders) {
        List<AgreedDraftOrderCollection> updatedCollection = emptyIfNull(caseData.getDraftOrdersWrapper().getAgreedDraftOrderCollection())
            .stream()
            .filter(d -> !hearingOrders.contains(d.getValue().getTargetDocument()))
            .toList();
        caseData.getDraftOrdersWrapper().setAgreedDraftOrderCollection(updatedCollection);
    }

    private void moveApprovedDocumentsToFinalisedOrder(FinremCaseData caseData, List<CaseDocument> hearingOrders) {
        removeDocumentFromsAgreedDraftOrderCollection(caseData, hearingOrders);
        Pair<List<PsaDocumentReview>, List<DraftOrderDocumentReview>> removed = removeDocumentFromDraftOrderReview(caseData, hearingOrders);
        populateRemovedOrdersToFinalisedOrder(caseData, removed);
    }

    private void populateRemovedOrdersToFinalisedOrder(FinremCaseData caseData,
                                                       Pair<List<PsaDocumentReview>, List<DraftOrderDocumentReview>> removed) {
        caseData.getDraftOrdersWrapper().setFinalisedOrdersCollection(concat(
            concat(
                defaultIfNull(caseData.getDraftOrdersWrapper().getFinalisedOrdersCollection(), new ArrayList<>()).stream(),
                removed.getRight().stream().map(this::toFinalisedOrderCollection)
            ),
            removed.getLeft().stream().map(this::toFinalisedOrderCollection)).toList());
    }

    private FinalisedOrderCollection toFinalisedOrderCollection(PsaDocumentReview psaDocumentReview) {
        return FinalisedOrderCollection.builder()
            .value(FinalisedOrder.builder()
                .submittedDate(psaDocumentReview.getSubmittedDate())
                .submittedBy(psaDocumentReview.getSubmittedBy())
                .finalisedDocument(psaDocumentReview.getTargetDocument())
                .finalOrder(psaDocumentReview.getFinalOrder())
                .approvalDate(psaDocumentReview.getApprovalDate())
                .approvalJudge(psaDocumentReview.getApprovalJudge())
                .build())
            .build();
    }

    private FinalisedOrderCollection toFinalisedOrderCollection(DraftOrderDocumentReview draftOrderDocumentReview) {
        return FinalisedOrderCollection.builder()
            .value(FinalisedOrder.builder()
                .attachments(draftOrderDocumentReview.getAttachments())
                .submittedDate(draftOrderDocumentReview.getSubmittedDate())
                .submittedBy(draftOrderDocumentReview.getSubmittedBy())
                .finalisedDocument(draftOrderDocumentReview.getTargetDocument())
                .finalOrder(draftOrderDocumentReview.getFinalOrder())
                .approvalDate(draftOrderDocumentReview.getApprovalDate())
                .approvalJudge(draftOrderDocumentReview.getApprovalJudge())
                .build())
            .build();
    }

    private void clearTemporaryFields(FinremCaseData caseData) {
        caseData.setAdditionalDocument(null);
        caseData.setOrdersToShare(null);
    }

    private void setConsolidateView(FinremCaseDetails caseDetails,
                                    List<String> partyList) {
        Long caseId = caseDetails.getId();
        log.info("Setting Documents for Case ID: {}", caseId);
        sendOrderPartyDocumentList.forEach(handler -> handler.setUpOrderDocumentsOnPartiesTab(caseDetails, partyList));
    }

    private void shareAndSendHearingDocuments(FinremCaseDetails caseDetails,
                                              List<CaseDocument> hearingOrders,
                                              List<String> partyList,
                                              List<OrderSentToPartiesCollection> printOrderCollection,
                                              String userAuthorisation) {
        Long caseId = caseDetails.getId();
        log.info("Share Hearing Documents for Case ID: {}", caseId);
        List<CaseDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, userAuthorisation);
        hearingDocumentPack.forEach(doc -> printOrderCollection.add(addToPrintOrderCollection(doc)));
        sendOrderPartyDocumentList.forEach(handler -> handler.setUpOrderDocumentsOnCase(caseDetails, partyList, hearingDocumentPack));
    }

    private List<CaseDocument> createHearingDocumentPack(FinremCaseDetails caseDetails,
                                                         List<CaseDocument> hearingOrders,
                                                         String authorisationToken) {
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Creating hearing document pack for caseId {}", caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<CaseDocument> orders = new ArrayList<>(hearingOrders);
        if (caseData.getOrderApprovedCoverLetter() == null) {
            throw new IllegalStateException("orderApprovedCoverLetter is missing unexpectedly");
        }
        orders.add(caseData.getOrderApprovedCoverLetter());

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(orders::add);
        }

        List<CaseDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsPdfDocuments(caseDetails, authorisationToken);
        if (!otherHearingDocuments.isEmpty()) {
            orders.addAll(otherHearingDocuments);
        }
        return orders;
    }

    private void shareAndSendGeneralOrderWithSelectedParties(FinremCaseDetails caseDetails,
                                                             List<String> partyList,
                                                             DynamicMultiSelectList selectedOrders,
                                                             List<OrderSentToPartiesCollection> printOrderCollection) {
        Long caseId = caseDetails.getId();
        log.info("Share selected 'GeneralOrder' With selected parties for caseId {}", caseId);

        FinremCaseData caseData = caseDetails.getData();

        List<ContestedGeneralOrderCollection> generalOrders = caseData.getGeneralOrderWrapper().getGeneralOrders();

        if (generalOrders != null && !generalOrders.isEmpty()) {
            generalOrders.forEach(go -> {
                ContestedGeneralOrder contestedGeneralOrder = go.getValue();
                if (contestedGeneralOrder != null && contestedGeneralOrder.getAdditionalDocument() != null) {
                    CaseDocument generalOrder = contestedGeneralOrder.getAdditionalDocument();
                    if (generalOrderService.isSelectedOrderMatches(selectedOrders, contestedGeneralOrder)) {
                        sendOrderPartyDocumentList.forEach(
                            handler -> handler.setUpOrderDocumentsOnCase(caseDetails, partyList, List.of(generalOrder)));
                        printOrderCollection.add(addToPrintOrderCollection(generalOrder));
                    }
                }
            });
        }
    }

    private void stampAndAddToCollection(FinremCaseDetails caseDetails, CaseDocument latestHearingOrder,
                                         String authToken) {
        String caseId = String.valueOf(caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();
        List<DirectionOrderCollection> finalOrderCollection
            = dateService.addCreatedDateInFinalOrder(caseData.getFinalOrderCollection(), authToken);
        if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, latestHearingOrder)) {
            AtomicReference<YesOrNo> result = isOrderAlreadyStamped(caseData, latestHearingOrder);
            if (result.get() == null || result.get().equals(YesOrNo.NO)) {
                StampType stampType = documentHelper.getStampType(caseData);
                CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType, caseId);
                log.info("Stamped Documents = {} for caseId {}", stampedDocs, caseId);
                finalOrderCollection.add(prepareFinalOrderList(stampedDocs));
                log.info("If Existing final order collection = {}", finalOrderCollection);
            }
            caseData.setFinalOrderCollection(finalOrderCollection);
            log.info("Finished stamping final order for caseId {}", caseId);
        } else {
            caseData.setFinalOrderCollection(finalOrderCollection);
            log.info("Finished stamping else final order for caseId {}", caseId);
        }
    }

    private AtomicReference<YesOrNo> isOrderAlreadyStamped(FinremCaseData caseData, CaseDocument latestHearingOrder) {
        List<DirectionOrderCollection> hearingOrders = caseData.getUploadHearingOrder();
        AtomicReference<YesOrNo> result = new AtomicReference<>();
        if (hearingOrders != null && !hearingOrders.isEmpty()) {
            hearingOrders.forEach(order -> {
                CaseDocument document = order.getValue().getUploadDraftDocument();
                if (latestHearingOrder.getDocumentFilename().equals(
                    document.getDocumentFilename())) {
                    result.set(order.getValue().getIsOrderStamped());
                }
            });
        }
        return result;
    }

    private OrderSentToPartiesCollection addToPrintOrderCollection(CaseDocument document) {
        return OrderSentToPartiesCollection.builder()
            .value(SendOrderDocuments.builder().caseDocument(document).build())
            .build();
    }

    private DirectionOrderCollection prepareFinalOrderList(CaseDocument document) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(document)
                .orderDateTime(LocalDateTime.now())
                .isOrderStamped(YesOrNo.YES)
                .build())
            .build();
    }
}
