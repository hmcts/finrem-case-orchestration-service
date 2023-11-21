package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderSentToPartiesCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.SendOrderDocuments;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OrderDateService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.sendorder.SendOrderPartyDocumentHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
@Service
public class SendOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList;
    private final OrderDateService dateService;


    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService,
                                                  GenericDocumentService genericDocumentService,
                                                  DocumentHelper documentHelper,
                                                  List<SendOrderPartyDocumentHandler> sendOrderPartyDocumentList,
                                                  OrderDateService dateService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.sendOrderPartyDocumentList = sendOrderPartyDocumentList;
        this.dateService =  dateService;
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
        log.info("Invoking contested event {}, callback {} callback for case id: {}",
            EventType.SEND_ORDER, CallbackType.ABOUT_TO_SUBMIT, caseId);

        try {
            FinremCaseData caseData = caseDetails.getData();
            List<String> parties = generalOrderService.getParties(caseDetails);
            log.info("selected parties {} on case {}", parties, caseId);

            DynamicMultiSelectList selectedOrders = caseData.getOrdersToShare();
            log.info("selected orders {} on case {}", selectedOrders, caseId);

            List<OrderSentToPartiesCollection> printOrderCollection = new ArrayList<>();
            CaseDocument document = caseData.getAdditionalDocument();
            if (document != null) {
                log.info("additional uploaded document with send order {} for caseId {}", document, caseId);
                CaseDocument additionalUploadedOrderDoc = genericDocumentService.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId);
                printOrderCollection.add(addToPrintOrderCollection(additionalUploadedOrderDoc));
                caseData.setAdditionalDocument(additionalUploadedOrderDoc);
            }

            log.info("Share and print general with for case {}", caseDetails.getId());
            shareAndSendGeneralOrderWithSelectedParties(caseDetails, parties, selectedOrders, printOrderCollection);


            log.info("Share and print hearing order for case {}", caseDetails.getId());
            List<CaseDocument> hearingOrders = generalOrderService.hearingOrdersToShare(caseDetails, selectedOrders);
            if (hearingOrders != null && !hearingOrders.isEmpty()) {
                shareAndSendHearingDocuments(caseDetails, hearingOrders, parties, printOrderCollection, userAuthorisation);
                log.info("sending for stamp final order on case {}", caseDetails.getId());
                hearingOrders.forEach(orderToStamp -> {
                    log.info("StampFinalOrder {} for Case ID {}, ", orderToStamp, caseId);
                    stampAndAddToCollection(caseDetails, orderToStamp, userAuthorisation);
                });
            }
            caseData.setOrdersSentToPartiesCollection(printOrderCollection);
            caseData.setAdditionalDocument(null);
            setConsolidateView(caseDetails, parties);
        } catch (RuntimeException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }

    private void setConsolidateView(FinremCaseDetails caseDetails,
                                    List<String> partyList) {
        Long caseId = caseDetails.getId();
        log.info("setting Documents for case {}:", caseId);
        sendOrderPartyDocumentList.forEach(
            handler -> handler.setUpOrderDocumentsOnPartiesTab(caseDetails, partyList));
    }


    private void shareAndSendHearingDocuments(FinremCaseDetails caseDetails,
                                              List<CaseDocument> hearingOrders,
                                              List<String> partyList,
                                              List<OrderSentToPartiesCollection> printOrderCollection,
                                              String userAuthorisation) {
        Long caseId = caseDetails.getId();
        log.info("Share Hearing Documents for case {}:", caseId);
        List<CaseDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, userAuthorisation);
        hearingDocumentPack.forEach(doc -> printOrderCollection.add(addToPrintOrderCollection(doc)));
        sendOrderPartyDocumentList.forEach(
            handler -> handler.setUpOrderDocumentsOnCase(caseDetails, partyList, hearingDocumentPack));

    }

    private List<CaseDocument> createHearingDocumentPack(FinremCaseDetails caseDetails,
                                                         List<CaseDocument> hearingOrders,
                                                         String authorisationToken) {

        String caseId = String.valueOf(caseDetails.getId());
        log.info("Creating hearing document pack for caseId {}", caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<CaseDocument> orders = new ArrayList<>(hearingOrders);
        orders.add(caseData.getOrderApprovedCoverLetter());

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                orders::add);
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
        CaseDocument generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

        if (generalOrderService.isSelectedOrderMatches(selectedOrders, generalOrder)) {
            sendOrderPartyDocumentList.forEach(
                handler -> handler.setUpOrderDocumentsOnCase(caseDetails, partyList, List.of(generalOrder)));
            printOrderCollection.add(addToPrintOrderCollection(generalOrder));
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
