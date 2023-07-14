package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.BULK_PRINT_LETTER_ID_APP;


@Slf4j
@Service
public class SendConsentOrderInContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final NotificationService notificationService;


    public SendConsentOrderInContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
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
            && EventType.SEND_CONSENT_IN_CONTESTED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested event {}, callback {} for case id {}",
            callbackRequest.getEventType(), CallbackType.ABOUT_TO_SUBMIT, caseId);

        try {
            FinremCaseData caseData = caseDetails.getData();
            List<String> parties = generalOrderService.getParties(caseDetails);
            log.info("Selected parties {} on case id {}", parties, caseId);

            DynamicMultiSelectList selectedConsentOrders = caseData.getConsentInContestedOrdersToShare();
            log.info("Selected consent orders {} on case id {}", selectedConsentOrders, caseId);

            CaseDocument document = caseData.getAdditionalConsentInContestedDocument();
            if (document != null) {
                log.info("Additional uploaded document {} to be sent with consent order for case id {}", document, caseId);
                caseData.setAdditionalConsentInContestedDocument(
                    genericDocumentService.convertDocumentIfNotPdfAlready(document, userAuthorisation, caseId));
            }

            shareAndSendConsentOrderWithSelectedParties(caseDetails, parties, selectedConsentOrders, userAuthorisation);

        } catch (RuntimeException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
                .data(caseDetails.getData()).errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseDetails.getData()).build();
    }


//    private void shareAndSendHearingDocuments(FinremCaseDetails caseDetails,
//                                              List<CaseDocument> hearingOrders,
//                                              List<String> partyList,
//                                              String userAuthorisation) {
//        Long caseId = caseDetails.getId();
//
//        List<CaseDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, userAuthorisation);
//        List<BulkPrintDocument> bulkPrintPack = documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingDocumentPack);
//
//        FinremCaseData caseData = caseDetails.getData();
//        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
//            log.info("Received request to send hearing pack to applicant for case {}:", caseId);
//            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getAppOrderCollection())
//                .orElse(new ArrayList<>());
//            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
//            caseData.setAppOrderCollection(orderColl);
//            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
//                log.info("Sending hearing pack to applicant for caseId {}", caseId);
//                bulkPrintService.printApplicantDocuments(caseDetails, userAuthorisation, bulkPrintPack);
//            }
//        }
//
//        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
//            log.info("Received request to send hearing pack to respondent for case {}:", caseId);
//            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getRespOrderCollection())
//                .orElse(new ArrayList<>());
//            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
//            caseData.setRespOrderCollection(orderColl);
//            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
//                log.info("Sending hearing pack to respondent for caseId {}", caseId);
//                bulkPrintService.printRespondentDocuments(caseDetails, userAuthorisation, bulkPrintPack);
//            }
//        }
//
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
//            log.info("Received request to send hearing pack to intervener1 for case {}:", caseId);
//            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
//                .orElse(new ArrayList<>());
//            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
//            caseData.setIntv1OrderCollection(orderColl);
//            //send to bulk print
//        }
//
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
//            log.info("Received request to send hearing pack to intervener2 for case {}:", caseId);
//            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
//                .orElse(new ArrayList<>());
//            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
//            caseData.setIntv2OrderCollection(orderColl);
//            //send to bulk print
//        }
//
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
//            log.info("Received request to send hearing pack to intervener3 for case {}:", caseId);
//            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
//                .orElse(new ArrayList<>());
//            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
//            caseData.setIntv3OrderCollection(orderColl);
//            //send to bulk print
//        }
//
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
//            log.info("Received request to send hearing pack to intervener4 for case {}:", caseId);
//            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
//                .orElse(new ArrayList<>());
//            hearingDocumentPack.forEach(document -> orderColl.add(getApprovedOrderCollection(document)));
//            caseData.setIntv4OrderCollection(orderColl);
//            //send to bulk print
//        }
//
//    }
//
//    private List<CaseDocument> createHearingDocumentPack(FinremCaseDetails caseDetails,
//                                                         List<CaseDocument> hearingOrders,
//                                                         String authorisationToken) {
//
//        String caseId = String.valueOf(caseDetails.getId());
//        log.info("Creating hearing document pack for caseId {}", caseId);
//        FinremCaseData caseData = caseDetails.getData();
//        List<CaseDocument> orders = new ArrayList<>(hearingOrders);
//        orders.add(caseData.getOrderApprovedCoverLetter());
//        CaseDocument document = caseData.getAdditionalDocument();
//        if (document != null) {
//            orders.add(document);
//        }
//
//        if (documentHelper.hasAnotherHearing(caseData)) {
//            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
//            latestAdditionalHearingDocument.ifPresent(
//                orders::add);
//        }
//
//        List<CaseDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsPdfDocuments(caseDetails, authorisationToken);
//        if (!otherHearingDocuments.isEmpty()) {
//            orders.addAll(otherHearingDocuments);
//        }
//        return orders;
//    }


    private void shareAndSendConsentOrderWithSelectedParties(FinremCaseDetails caseDetails,
                                                             List<String> partyList,
                                                             DynamicMultiSelectList selectedConsentOrders,
                                                             String userAuthorisation) {

        Long caseId = caseDetails.getId();
        log.info("Sharing selected consent order with selected parties for case id {}", caseId);

        FinremCaseData caseData = caseDetails.getData();
        List<ConsentOrderCollection> consentOrders = caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders();

        consentOrders.forEach(order -> {
            if (isSelectedOrderMatches(selectedConsentOrders, order.getApprovedOrder().getConsentOrder())) {
                shareAndPrintConsentOrderWithApplicant(caseDetails, partyList, userAuthorisation, caseId, caseData,
                    order.getApprovedOrder().getConsentOrder());
                shareAndPrintConsentOrderWithRespondent(caseDetails, partyList, userAuthorisation, caseId, caseData,
                    order.getApprovedOrder().getConsentOrder());
                shareAndPrintConsentOrderWithIntervenerOne(partyList, caseId, caseData,
                    order.getApprovedOrder().getConsentOrder());
                shareAndPrintConsentOrderWithIntervenerTwo(partyList, caseId, caseData,
                    order.getApprovedOrder().getConsentOrder());
                shareAndPrintConsentOrderWithIntervenerThree(partyList, caseId, caseData,
                    order.getApprovedOrder().getConsentOrder());
                shareAndPrintConsentOrderWithIntervenerFour(partyList, caseId, caseData,
                    order.getApprovedOrder().getConsentOrder());
            }
        });
    }

//    public void sendConsentOrderToBulkPrint(CaseDetails caseDetails, String authorisationToken) {
//        Map<String, Object> caseData = caseDetails.getData();
//
//        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
//            log.info("Sending approved order for applicant to bulk print for case {}", caseDetails.getId());
//            UUID applicantLetterId = shouldPrintOrderApprovedDocuments(caseDetails, authorisationToken)
//                ? printApplicantConsentOrderApprovedDocuments(caseDetails, authorisationToken)
//                : printApplicantConsentOrderNotApprovedDocuments(caseDetails, authorisationToken);
//            caseData.put(BULK_PRINT_LETTER_ID_APP, applicantLetterId);
//        }
//
//        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
//            log.info("Sending approved order for respondent to bulk print for case {}", caseDetails.getId());
//            generateCoversheetForRespondentAndSendOrders(caseDetails, authorisationToken);
//        }
//    }

    private void shareAndPrintConsentOrderWithIntervenerFour(List<String> partyList, Long caseId,
                                                      FinremCaseData caseData, CaseDocument consentOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            log.info("Received request to send general to intervener4 for case {}", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, consentOrder, orderColl);
            caseData.setIntv4OrderCollection(orderColl);
            log.info("Sending documents {} to intervener4 for case {}", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintConsentOrderWithIntervenerThree(List<String> partyList, Long caseId,
                                                       FinremCaseData caseData, CaseDocument consentOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            log.info("Received request to send general to intervener3 for case {}", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, consentOrder, orderColl);
            caseData.setIntv3OrderCollection(orderColl);
            log.info("Sending documents {} to intervener3 for case {}", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintConsentOrderWithIntervenerTwo(List<String> partyList, Long caseId,
                                                     FinremCaseData caseData, CaseDocument consentOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            log.info("Received request to send general to intervener2 for case {}", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, consentOrder, orderColl);
            caseData.setIntv2OrderCollection(orderColl);
            log.info("Sending documents {} to intervener2 for case {}", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintConsentOrderWithIntervenerOne(List<String> partyList, Long caseId,
                                                     FinremCaseData caseData, CaseDocument consentOrder) {
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            log.info("Received request to send consent order to intervener1 for case {}", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, consentOrder, orderColl);
            caseData.setIntv1OrderCollection(orderColl);
            log.info("Sending documents {} to intervener1 for case {}", documents, caseId);
            //send documents to bulk print
        }
    }

    private void shareAndPrintConsentOrderWithRespondent(FinremCaseDetails caseDetails, List<String> partyList,
                                                         String userAuthorisation, Long caseId,
                                                         FinremCaseData caseData, CaseDocument consentOrder) {
        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
            log.info("Received request to send consent order to respondent for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getRespOrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, consentOrder, orderColl);
            caseData.setRespOrderCollection(orderColl);
            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending consent order to respondent for case {}", caseId);
                bulkPrintService.printRespondentDocuments(caseDetails,
                    userAuthorisation,
                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
            }
        }
    }

    private void shareAndPrintConsentOrderWithApplicant(FinremCaseDetails caseDetails,
                                                        List<String> partyList, String userAuthorisation,
                                                        Long caseId, FinremCaseData caseData, CaseDocument consentOrder) {
        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            log.info("Received request to send consent order to applicant for case {}:", caseId);
            List<ApprovedOrderCollection> orderColl = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            List<CaseDocument> documents = addOrderToCollection(caseData, consentOrder, orderColl);
            caseData.setAppOrderCollection(orderColl);
            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                log.info("Sending consent order to applicant for caseId {}", caseId);
                bulkPrintService.printApplicantDocuments(caseDetails,
                    userAuthorisation,
                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
            }
        }
    }

    private List<CaseDocument> addOrderToCollection(FinremCaseData caseData,
                                                    CaseDocument consentOrder, List<ApprovedOrderCollection> orderColl) {
        List<CaseDocument> documentsToPrint = new ArrayList<>();
        orderColl.add(getApprovedOrderCollection(consentOrder));
        documentsToPrint.add(consentOrder);
        CaseDocument document = caseData.getAdditionalDocument();
        if (document != null) {
            orderColl.add(getApprovedOrderCollection(document));
            documentsToPrint.add(document);
        }
        return documentsToPrint;
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument consentOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(consentOrder)
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

//    private void stampAndAddToCollection(FinremCaseDetails caseDetails, CaseDocument latestHearingOrder,
//                                         String authToken) {
//        String caseId = String.valueOf(caseDetails.getId());
//        FinremCaseData caseData = caseDetails.getData();
//
//        StampType stampType = documentHelper.getStampType(caseData);
//        CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType, caseId);
//        log.info("Stamped Documents = {} for caseId {}", stampedDocs, caseId);
//
//        List<DirectionOrderCollection> finalOrderCollection = Optional.ofNullable(caseData.getFinalOrderCollection())
//            .orElse(new ArrayList<>());
//
//        finalOrderCollection.add(prepareFinalOrderList(stampedDocs));
//        log.info("Existing final order collection = {}", finalOrderCollection);
//
//        caseData.setFinalOrderCollection(finalOrderCollection);
//        log.info("Finished stamping final order for caseId {}", caseId);
//    }

//    private DirectionOrderCollection prepareFinalOrderList(CaseDocument document) {
//        return DirectionOrderCollection.builder()
//            .value(DirectionOrder.builder().uploadDraftDocument(document).build())
//            .build();
//    }
}
