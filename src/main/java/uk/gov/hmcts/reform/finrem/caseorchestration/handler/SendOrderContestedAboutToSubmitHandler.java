package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ApprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
public class SendOrderContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;


    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService,
                                                  GenericDocumentService genericDocumentService,
                                                  DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
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

            List<CaseDocument> ordersSentToPartiesCollection = new ArrayList<>();
            CaseDocument document = caseData.getAdditionalDocument();
            if (document != null) {
                log.info("additional uploaded document with send order {} for caseId {}", document, caseId);
                CaseDocument additionalUploadedOrderDoc = genericDocumentService.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId);
                ordersSentToPartiesCollection.add(additionalUploadedOrderDoc);
                caseData.setAdditionalDocument(additionalUploadedOrderDoc);
            }

            log.info("Share and print general with for case {}", caseDetails.getId());
            shareAndSendGeneralOrderWithSelectedParties(caseDetails, parties, selectedOrders, ordersSentToPartiesCollection);


            log.info("Share and print hearing order for case {}", caseDetails.getId());
            List<CaseDocument> hearingOrders = generalOrderService.hearingOrdersToShare(caseDetails, selectedOrders);
            if (hearingOrders != null && !hearingOrders.isEmpty()) {
                shareAndSendHearingDocuments(caseDetails, hearingOrders, parties, ordersSentToPartiesCollection, userAuthorisation);
                log.info("sending for stamp final order on case {}", caseDetails.getId());
                hearingOrders.forEach(orderToStamp -> {
                    log.info("StampFinalOrder {} for Case ID {}, ", orderToStamp, caseId);
                    stampAndAddToCollection(caseDetails, orderToStamp, userAuthorisation);
                });
            }
            caseData.setOrdersSentToPartiesCollection(ordersSentToPartiesCollection);
        } catch (RuntimeException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }


    private void shareAndSendHearingDocuments(FinremCaseDetails caseDetails,
                                              List<CaseDocument> hearingOrders,
                                              List<String> partyList,
                                              List<CaseDocument> ordersSentToPartiesCollection,
                                              String userAuthorisation) {
        Long caseId = caseDetails.getId();
        log.info("Share Hearing Documents for case {}:", caseId);
        List<CaseDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, userAuthorisation);
        ordersSentToPartiesCollection.addAll(hearingDocumentPack);

        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            log.info("Received request to send hearing pack to applicant for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setAppOrderCollection(orderColl);
        }

        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            log.info("Received request to send hearing pack to respondent for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getRespOrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setRespOrderCollection(orderColl);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            log.info("Received request to send hearing pack to intervener1 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv1OrderCollection(orderColl);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            log.info("Received request to send hearing pack to intervener2 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv2OrderCollection(orderColl);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            log.info("Received request to send hearing pack to intervener3 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv3OrderCollection(orderColl);
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            log.info("Received request to send hearing pack to intervener4 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv4OrderCollection(orderColl);
        }

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
                                                             List<CaseDocument> ordersSentToPartiesCollection) {

        Long caseId = caseDetails.getId();
        log.info("Share selected 'GeneralOrder' With selected parties for caseId {}", caseId);

        FinremCaseData caseData = caseDetails.getData();
        CaseDocument generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

        if (generalOrderService.isSelectedOrderMatches(selectedOrders, generalOrder)) {
            shareAndPrintGeneralOrderWithApplicant(caseDetails, partyList, generalOrder);
            shareAndPrintGeneralOrderWithRespondent(caseDetails, partyList, generalOrder);
            shareAndPrintOrderWithIntervenerOne(caseDetails, partyList, generalOrder);
            shareAndPrintOrderWithIntervenerTwo(caseDetails, partyList, generalOrder);
            shareAndPrintOrderWithIntervenerThree(caseDetails, partyList, generalOrder);
            shareAndPrintOrderWithIntervenerFour(caseDetails, partyList, generalOrder);
            ordersSentToPartiesCollection.add(generalOrder);
        }
    }

    private void shareAndPrintOrderWithIntervenerFour(FinremCaseDetails caseDetails,
                                                      List<String> partyList,
                                                      CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            final Long caseId = caseDetails.getId();
            FinremCaseData caseData = caseDetails.getData();
            log.info("Received request to send general to intervener4 for case {}:", caseId);
            List<ApprovedOrderCollection> intv4ApprovedOrderCollections = Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());
            intv4ApprovedOrderCollections.add(getApprovedOrderCollection(generalOrder));
            caseData.setIntv4OrderCollection(intv4ApprovedOrderCollections);
        }
    }

    private void shareAndPrintOrderWithIntervenerThree(FinremCaseDetails caseDetails,
                                                       List<String> partyList,
                                                       CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            final Long caseId = caseDetails.getId();
            FinremCaseData caseData = caseDetails.getData();
            log.info("Received request to send general to intervener3 for case {}:", caseId);
            List<ApprovedOrderCollection> intv3ApprovedOrderCollections = Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());
            intv3ApprovedOrderCollections.add(getApprovedOrderCollection(generalOrder));
            caseData.setIntv3OrderCollection(intv3ApprovedOrderCollections);
        }
    }

    private void shareAndPrintOrderWithIntervenerTwo(FinremCaseDetails caseDetails,
                                                     List<String> partyList,
                                                     CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            final Long caseId = caseDetails.getId();
            FinremCaseData caseData = caseDetails.getData();
            log.info("Received request to send general to intervener2 for case {}:", caseId);
            List<ApprovedOrderCollection> intv2ApprovedOrderCollections = Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());
            intv2ApprovedOrderCollections.add(getApprovedOrderCollection(generalOrder));
            caseData.setIntv2OrderCollection(intv2ApprovedOrderCollections);
        }
    }

    private void shareAndPrintOrderWithIntervenerOne(FinremCaseDetails caseDetails,
                                                     List<String> partyList,
                                                     CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            final Long caseId = caseDetails.getId();
            FinremCaseData caseData = caseDetails.getData();
            log.info("Received request to send general to intervener1 for case {}:", caseId);
            List<ApprovedOrderCollection> intv1ApprovedOrderCollections = Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());
            intv1ApprovedOrderCollections.add(getApprovedOrderCollection(generalOrder));
            caseData.setIntv1OrderCollection(intv1ApprovedOrderCollections);
        }
    }

    private void shareAndPrintGeneralOrderWithRespondent(FinremCaseDetails caseDetails,
                                                         List<String> partyList,
                                                         CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            final Long caseId = caseDetails.getId();
            FinremCaseData caseData = caseDetails.getData();
            log.info("Received request to send general to respondent for case {}:", caseId);
            List<ApprovedOrderCollection> respApprovedOrderCollections = Optional.ofNullable(caseData.getRespOrderCollection())
                .orElse(new ArrayList<>());
            respApprovedOrderCollections.add(getApprovedOrderCollection(generalOrder));
            caseData.setRespOrderCollection(respApprovedOrderCollections);
        }
    }

    private void shareAndPrintGeneralOrderWithApplicant(FinremCaseDetails caseDetails,
                                                        List<String> partyList,
                                                        CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            final Long caseId = caseDetails.getId();
            FinremCaseData caseData = caseDetails.getData();
            log.info("Received request to send general to applicant for case {}:", caseId);
            List<ApprovedOrderCollection> appApprovedOrderCollections = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            appApprovedOrderCollections.add(getApprovedOrderCollection(generalOrder));
            caseData.setAppOrderCollection(appApprovedOrderCollections);
        }
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument generalOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(generalOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }



    private void stampAndAddToCollection(FinremCaseDetails caseDetails, CaseDocument latestHearingOrder,
                                         String authToken) {
        String caseId = String.valueOf(caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType, caseId);
        log.info("Stamped Documents = {} for caseId {}", stampedDocs, caseId);

        List<DirectionOrderCollection> finalOrderCollection = Optional.ofNullable(caseData.getFinalOrderCollection())
            .orElse(new ArrayList<>());

        finalOrderCollection.add(prepareFinalOrderList(stampedDocs));
        log.info("Existing final order collection = {}", finalOrderCollection);

        caseData.setFinalOrderCollection(finalOrderCollection);
        log.info("Finished stamping final order for caseId {}", caseId);
    }

    private DirectionOrderCollection prepareFinalOrderList(CaseDocument document) {
        return DirectionOrderCollection.builder()
            .value(DirectionOrder.builder().uploadDraftDocument(document).build())
            .build();
    }
}
