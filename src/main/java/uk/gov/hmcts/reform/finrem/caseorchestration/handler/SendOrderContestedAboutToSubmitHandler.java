package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.AttachmentToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShare;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrderToShareCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.OrdersToSend;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.SendOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.DraftOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderDateService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.SendOrdersCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderPartyDocumentHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.ListUtils.defaultIfNull;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.SEND_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;

@Slf4j
@Service
public class SendOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final DraftOrderService draftOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList;
    private final OrderDateService dateService;
    private final SendOrdersCategoriser sendOrdersCategoriser;

    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService, DraftOrderService draftOrderService,
                                                  GenericDocumentService genericDocumentService,
                                                  DocumentHelper documentHelper,
                                                  List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList,
                                                  OrderDateService dateService,
                                                  SendOrdersCategoriser sendOrdersCategoriser) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.draftOrderService = draftOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.sendOrderPartyDocumentList = sendOrderPartyDocumentList;
        this.dateService = dateService;
        this.sendOrdersCategoriser = sendOrdersCategoriser;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return ABOUT_TO_SUBMIT.equals(callbackType) && CONTESTED.equals(caseType) && SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = getCaseId(caseDetails);
        log.info("Invoking contested event {}, callback {} callback for Case ID: {}", callbackRequest.getEventType(), ABOUT_TO_SUBMIT, caseId);

        FinremCaseData caseData = caseDetails.getData();
        try {
            List<String> parties = generalOrderService.getParties(caseDetails);
            List<OrderToShare> selectedOrders = getSelectedOrders(caseData.getSendOrderWrapper());

            logSelectedPartiesAndOrders(parties, selectedOrders, caseId);

            List<OrderSentToPartiesCollection> printOrderCollection = new ArrayList<>();

            handleAdditionalDocumentUploadedInSendOrderEvent(caseDetails, printOrderCollection, userAuthorisation);
            shareAndSendGeneralOrderWithSelectedParties(caseDetails, parties, selectedOrders, printOrderCollection);

            Triple<List<CaseDocument>, List<CaseDocument>, Map<CaseDocument, List<CaseDocument>>> hearingOrders
                = generalOrderService.hearingOrdersToShare(caseDetails, selectedOrders);
            List<CaseDocument> legacyHearingOrders = hearingOrders.getLeft();
            List<CaseDocument> newProcessedOrders = hearingOrders.getMiddle();
            Map<CaseDocument, List<CaseDocument>> order2AttachmentMap = hearingOrders.getRight();

            if (hasApprovedOrdersToBeSent(legacyHearingOrders, newProcessedOrders)) {
                List<CaseDocument> caseDocumentsToShare = concat(
                    concat(legacyHearingOrders.stream(), newProcessedOrders.stream()),
                    order2AttachmentMap.values().stream().flatMap(List::stream)
                ).toList();

                // Add order approved cover letter and add orders (legacy and new) to printOrderCollection
                shareAndSendHearingDocuments(caseDetails, caseDocumentsToShare, parties, printOrderCollection, userAuthorisation);

                stampLegacyHearingOrdersAndPopulateFinalOrderCollection(caseDetails, legacyHearingOrders, order2AttachmentMap, userAuthorisation);

                // handling processed orders
                moveApprovedDocumentsToFinalisedOrder(caseData, newProcessedOrders);
            }
            caseData.setOrdersSentToPartiesCollection(printOrderCollection);
            setConsolidateView(caseDetails, parties);

            resetFields(caseData.getDraftOrdersWrapper());
            clearTemporaryFields(caseData);
            sendOrdersCategoriser.categorise(caseDetails.getData());

            draftOrderService.clearEmptyOrdersInDraftOrdersReviewCollection(caseData);
        } catch (RuntimeException e) {
            // The purpose of this catch block is to make the exception message available in the error message box
            // And it doesn't let CCD to retry if we populate the exception message to `errors`
            log.error("FAIL: {} on Case ID: {}", e.getMessage(), getCaseId(caseDetails), e);

            resetFields(caseData.getDraftOrdersWrapper());
            clearTemporaryFields(caseData);
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

    private String getCaseId(FinremCaseDetails caseDetails) {
        return String.valueOf(caseDetails.getId());
    }

    private void handleAdditionalDocumentUploadedInSendOrderEvent(FinremCaseDetails caseDetails,
                                                                  List<OrderSentToPartiesCollection> printOrderCollection, String userAuthorisation) {
        FinremCaseData caseData = caseDetails.getData();
        CaseDocument document = caseData.getSendOrderWrapper().getAdditionalDocument();
        if (document != null) {
            log.info("Additional uploaded document with send order {} for Case ID: {}", document, getCaseId(caseDetails));
            CaseDocument additionalUploadedOrderDoc = genericDocumentService.convertDocumentIfNotPdfAlready(document,
                userAuthorisation, caseDetails.getCaseType());
            printOrderCollection.add(addToPrintOrderCollection(additionalUploadedOrderDoc));
            caseData.getSendOrderWrapper().setAdditionalDocument(additionalUploadedOrderDoc);
        }
    }

    private void stampLegacyHearingOrdersAndPopulateFinalOrderCollection(FinremCaseDetails caseDetails, List<CaseDocument> legacyHearingOrders,
                                                                         Map<CaseDocument, List<CaseDocument>> order2AttachmentMap,
                                                                         String userAuthorisation) {
        if (!legacyHearingOrders.isEmpty()) {
            log.info("Going to stamp legacy orders on Case ID: {}", getCaseId(caseDetails));
            // stamping legacy approved orders and add it to legacy finalised collection
            legacyHearingOrders.forEach(orderToStamp -> {
                log.info("Stamp and add to FinalOrderCollection {} for Case ID: {}, ", orderToStamp, getCaseId(caseDetails));
                stampAndAddToCollection(caseDetails, orderToStamp, order2AttachmentMap.get(orderToStamp), userAuthorisation);
            });
        }
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

        updatedCollection = emptyIfNull(caseData.getDraftOrdersWrapper().getIntvAgreedDraftOrderCollection())
            .stream()
            .filter(d -> !hearingOrders.contains(d.getValue().getTargetDocument()))
            .toList();
        caseData.getDraftOrdersWrapper().setIntvAgreedDraftOrderCollection(updatedCollection);
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
                .coverLetter(psaDocumentReview.getCoverLetter())
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
                .coverLetter(draftOrderDocumentReview.getCoverLetter())
                .build())
            .build();
    }

    private void clearTemporaryFields(FinremCaseData caseData) {
        caseData.getSendOrderWrapper().setAdditionalDocument(null);
        caseData.getSendOrderWrapper().setOrdersToSend(null);
    }

    private void resetFields(DraftOrdersWrapper draftOrdersWrapper) {
        if (CollectionUtils.isEmpty(draftOrdersWrapper.getFinalisedOrdersCollection())) {
            draftOrdersWrapper.setFinalisedOrdersCollection(null);
        }
        if (CollectionUtils.isEmpty(draftOrdersWrapper.getAgreedDraftOrderCollection())) {
            draftOrdersWrapper.setAgreedDraftOrderCollection(null);
        }
    }

    private void setConsolidateView(FinremCaseDetails caseDetails,
                                    List<String> partyList) {
        log.info("Setting Documents for Case ID: {}", getCaseId(caseDetails));
        sendOrderPartyDocumentList.forEach(handler -> handler.setUpOrderDocumentsOnPartiesTab(caseDetails, partyList));
    }

    private void shareAndSendHearingDocuments(FinremCaseDetails caseDetails,
                                              List<CaseDocument> hearingOrders,
                                              List<String> partyList,
                                              List<OrderSentToPartiesCollection> printOrderCollection,
                                              String userAuthorisation) {
        log.info("Share Hearing Documents for Case ID: {}", getCaseId(caseDetails));
        List<CaseDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, userAuthorisation);
        hearingDocumentPack.forEach(doc -> printOrderCollection.add(addToPrintOrderCollection(doc)));
        sendOrderPartyDocumentList.forEach(handler -> handler.setUpOrderDocumentsOnCase(caseDetails, partyList, hearingDocumentPack));
    }

    private List<CaseDocument> createHearingDocumentPack(FinremCaseDetails caseDetails,
                                                         List<CaseDocument> hearingOrders,
                                                         String authorisationToken) {
        log.info("Creating hearing document pack for caseId {}", getCaseId(caseDetails));
        FinremCaseData caseData = caseDetails.getData();

        List<CaseDocument> orders = new ArrayList<>(hearingOrders);
        if (caseData.getOrderApprovedCoverLetter() == null) {
            throw new IllegalStateException("orderApprovedCoverLetter is missing unexpectedly");
        }

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
                                                             List<OrderToShare> selectedOrders,
                                                             List<OrderSentToPartiesCollection> printOrderCollection) {
        log.info("Share selected 'GeneralOrder' With selected parties for Case Id:{}", getCaseId(caseDetails));

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

    private void stampAndAddToCollection(FinremCaseDetails caseDetails, CaseDocument latestHearingOrder, List<CaseDocument> additionalAttachments,
                                         String authToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<DirectionOrderCollection> finalOrderCollection = dateService.addCreatedDateInFinalOrder(caseData.getFinalOrderCollection(), authToken);
        if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, latestHearingOrder)) {
            AtomicReference<YesOrNo> result = isOrderAlreadyStamped(caseData, latestHearingOrder);
            if (result.get() == null || result.get().equals(YesOrNo.NO)) {
                StampType stampType = documentHelper.getStampType(caseData);
                CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType,
                    caseDetails.getCaseType());
                log.info("Stamped Documents = {} for caseId {}", stampedDocs, getCaseId(caseDetails));
                finalOrderCollection.add(prepareFinalOrderList(stampedDocs, additionalAttachments));
                log.info("If Existing final order collection = {}", finalOrderCollection);
            }
            caseData.setFinalOrderCollection(finalOrderCollection);
            log.info("Finished stamping final order for caseId {}", getCaseId(caseDetails));
        } else {
            caseData.setFinalOrderCollection(finalOrderCollection);
            log.info("Finished stamping else final order for caseId {}", getCaseId(caseDetails));
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

    private DirectionOrderCollection prepareFinalOrderList(CaseDocument document, List<CaseDocument> additionalDocuments) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(document)
                .orderDateTime(LocalDateTime.now())
                .isOrderStamped(YesOrNo.YES)
                .additionalDocuments(additionalDocuments == null || additionalDocuments.isEmpty() ? null : additionalDocuments.stream()
                    .map(a -> DocumentCollectionItem.builder().value(a).build()).toList())
                .build())
            .build();
    }

    private List<OrderToShare> getSelectedOrders(SendOrderWrapper sendOrderWrapper) {
        return emptyIfNull(
            Optional.ofNullable(sendOrderWrapper.getOrdersToSend()).orElse(OrdersToSend.builder().build()).getValue()
        ).stream()
            .map(OrderToShareCollection::getValue)
            .filter(orderToShare -> YesOrNo.isYes(orderToShare.getDocumentToShare()))
            .toList();
    }

    private String formatSelectedAttachment(OrderToShare o) {
        return emptyIfNull(o.getAttachmentsToShare()).stream()
            .map(AttachmentToShareCollection::getValue)
            .filter(a -> YesOrNo.isYes(a.getDocumentToShare()))
            .map(a -> a.getDocumentId() + "|" + a.getAttachmentName())
            .collect(Collectors.joining(","));
    }

    private String formatOrderToShare(OrderToShare o) {
        return format("(%s|%s)+[%s]", o.getDocumentId(), o.getDocumentName(), formatSelectedAttachment(o));
    }

    private String formatOrderToShareList(List<OrderToShare> selectedOrders) {
        return selectedOrders.stream().map(this::formatOrderToShare).collect(Collectors.joining(","));
    }

    private void logSelectedPartiesAndOrders(List<String> parties, List<OrderToShare> selectedOrders, String caseId) {
        log.info("FR_sendOrder({}) - sending orders: ({}) to parties: {}", caseId, formatOrderToShareList(selectedOrders), parties);
    }
}
