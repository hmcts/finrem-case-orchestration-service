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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
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
    private final BulkPrintService bulkPrintService;
    private final NotificationService notificationService;


    public SendOrderContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                  GeneralOrderService generalOrderService,
                                                  GenericDocumentService genericDocumentService,
                                                  DocumentHelper documentHelper,
                                                  BulkPrintService bulkPrintService,
                                                  NotificationService notificationService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.bulkPrintService = bulkPrintService;
        this.notificationService = notificationService;
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

            CaseDocument document = caseData.getAdditionalDocument();
            if (document != null) {
                log.info("additional uploaded document with send order {} for caseId {}", document, caseId);
                caseData.setAdditionalDocument(genericDocumentService.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId));
            }

            log.info("Share and print general with for case {}", caseDetails.getId());
            shareAndSendGeneralOrderWithSelectedParties(caseDetails, parties, selectedOrders, userAuthorisation);


            log.info("Share and print hearing order for case {}", caseDetails.getId());
            List<CaseDocument> hearingOrders = generalOrderService.hearingOrdersToShare(caseDetails, selectedOrders);
            if (!hearingOrders.isEmpty()) {
                shareAndSendHearingDocuments(caseDetails, hearingOrders, parties, userAuthorisation);
                log.info("sending for stamp final order on case {}", caseDetails.getId());
                hearingOrders.forEach(orderToStamp -> {
                    log.info("StampFinalOrder {} for Case ID {}, ", orderToStamp, caseId);
                    stampAndAddToCollection(caseDetails, orderToStamp, userAuthorisation);
                });
            }
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
                                              String userAuthorisation) {
        Long caseId = caseDetails.getId();

        List<CaseDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, userAuthorisation);
        List<BulkPrintDocument> bulkPrintPack = documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingDocumentPack);

        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            log.info("Received request to send hearing pack to applicant for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setAppOrderCollection(orderColl);
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending hearing pack to applicant for caseId {}", caseId);
                bulkPrintService.printApplicantDocuments(caseDetails, userAuthorisation, bulkPrintPack);
            }
        }

        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            log.info("Received request to send hearing pack to respondent for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getRespOrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setRespOrderCollection(orderColl);
            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending hearing pack to respondent for caseId {}", caseId);
                bulkPrintService.printRespondentDocuments(caseDetails, userAuthorisation, bulkPrintPack);
            }
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            log.info("Received request to send hearing pack to intervener1 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv1OrderCollection(orderColl);
            //send to bulk print
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            log.info("Received request to send hearing pack to intervener2 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv2OrderCollection(orderColl);
            //send to bulk print
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            log.info("Received request to send hearing pack to intervener3 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv3OrderCollection(orderColl);
            //send to bulk print
        }

        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            log.info("Received request to send hearing pack to intervener4 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());
            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
            caseData.setIntv4OrderCollection(orderColl);
            //send to bulk print
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
        CaseDocument document = caseData.getAdditionalDocument();
        if (document != null) {
            orders.add(document);
        }

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
                                                             String userAuthorisation) {

        Long caseId = caseDetails.getId();
        log.info("Share selected 'GeneralOrder' With selected parties for caseId {}", caseId);

        FinremCaseData caseData = caseDetails.getData();
        CaseDocument generalOrder = caseData.getGeneralOrderWrapper().getGeneralOrderLatestDocument();

        if (isSelectedOrderMatches(selectedOrders, generalOrder)) {
            shareAndPrintGeneralOrderWithApplicant(caseDetails, partyList, userAuthorisation, caseId, caseData, generalOrder);
            shareAndPrintGeneralOrderWithRespondent(caseDetails, partyList, userAuthorisation, caseId, caseData, generalOrder);
            shareAndPrintOrderWithIntervenerOne(partyList, caseId, caseData, generalOrder);
            shareAndPrintOrderWithIntervenerTwo(partyList, caseId, caseData, generalOrder);
            shareAndPrintOrderWithIntervenerThree(partyList, caseId, caseData, generalOrder);
            shareAndPrintOrderWithIntervenerFour(partyList, caseId, caseData, generalOrder);
        }
    }

    private void shareAndPrintOrderWithIntervenerFour(List<String> partyList, Long caseId,
                                                      FinremCaseData caseData, CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            log.info("Received request to send general to intervener4 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, generalOrder, orderColl);
            caseData.setIntv4OrderCollection(orderColl);
            log.info("send documents {} to intervener4 for case {}:", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintOrderWithIntervenerThree(List<String> partyList, Long caseId,
                                                       FinremCaseData caseData, CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            log.info("Received request to send general to intervener3 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, generalOrder, orderColl);
            caseData.setIntv3OrderCollection(orderColl);
            log.info("send documents {} to intervener3 for case {}:", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintOrderWithIntervenerTwo(List<String> partyList, Long caseId,
                                                     FinremCaseData caseData, CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            log.info("Received request to send general to intervener2 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, generalOrder, orderColl);
            caseData.setIntv2OrderCollection(orderColl);
            log.info("send documents {} to intervener2 for case {}:", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintOrderWithIntervenerOne(List<String> partyList, Long caseId,
                                                     FinremCaseData caseData, CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            log.info("Received request to send general to intervener1 for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, generalOrder, orderColl);
            caseData.setIntv1OrderCollection(orderColl);
            log.info("send documents {} to intervener1 for case {}:", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintGeneralOrderWithRespondent(FinremCaseDetails caseDetails, List<String> partyList,
                                                         String userAuthorisation, Long caseId,
                                                         FinremCaseData caseData, CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            log.info("Received request to send general to respondent for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getRespOrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, generalOrder, orderColl);
            caseData.setRespOrderCollection(orderColl);
            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending general order to respondent for caseId {}", caseId);
                bulkPrintService.printRespondentDocuments(caseDetails,
                    userAuthorisation,
                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
            }
        }
    }

    private void shareAndPrintGeneralOrderWithApplicant(FinremCaseDetails caseDetails,
                                                        List<String> partyList, String userAuthorisation,
                                                        Long caseId, FinremCaseData caseData, CaseDocument generalOrder) {
        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            log.info("Received request to send general to applicant for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, generalOrder, orderColl);
            caseData.setAppOrderCollection(orderColl);
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending general order to applicant for caseId {}", caseId);
                bulkPrintService.printApplicantDocuments(caseDetails,
                    userAuthorisation,
                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
            }
        }
    }

    private List<CaseDocument> addOrderToCollection(FinremCaseData caseData,
                                                    CaseDocument generalOrder, List<ApprovedOrderCollection> orderColl) {
        List<CaseDocument> documentsToPrint = new ArrayList<>();
        orderColl.add(getApprovedOrderCollection(generalOrder));
        documentsToPrint.add(generalOrder);
        CaseDocument document = caseData.getAdditionalDocument();
        if (document != null) {
            orderColl.add(getApprovedOrderCollection(document));
            documentsToPrint.add(document);
        }
        return documentsToPrint;
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument generalOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(generalOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private boolean isSelectedOrderMatches(DynamicMultiSelectList selectedDocs, CaseDocument caseDocument) {
        if (caseDocument != null) {
            Optional<DynamicMultiSelectListElement> listElement = selectedDocs.getValue().stream()
                .filter(e -> e.getCode().equals(caseDocument.getDocumentFilename())).findAny();
            return listElement.isPresent();
        }
        return false;
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
