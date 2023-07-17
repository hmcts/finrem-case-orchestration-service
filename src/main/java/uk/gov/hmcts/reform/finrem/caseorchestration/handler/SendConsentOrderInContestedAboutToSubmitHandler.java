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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SendConsentOrderInContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final NotificationService notificationService;
    private final ConsentOrderPrintService consentOrderPrintService;


    public SendConsentOrderInContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           GeneralOrderService generalOrderService,
                                                           GenericDocumentService genericDocumentService,
                                                           DocumentHelper documentHelper,
                                                           BulkPrintService bulkPrintService,
                                                           NotificationService notificationService,
                                                           ConsentOrderPrintService consentOrderPrintService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.bulkPrintService = bulkPrintService;
        this.notificationService = notificationService;
        this.consentOrderPrintService = consentOrderPrintService;
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

    private void shareAndSendConsentOrderWithSelectedParties(FinremCaseDetails caseDetails,
                                                             List<String> partyList,
                                                             DynamicMultiSelectList selectedConsentOrders,
                                                             String userAuthorisation) {

        Long caseId = caseDetails.getId();
        log.info("Sharing selected consent order with selected parties for case id {}", caseId);

        FinremCaseData caseData = caseDetails.getData();
        ConsentOrderWrapper wrapper = caseData.getConsentOrderWrapper();
        List<CaseDocument> approvedConsentOrders = new ArrayList<>();
        List<CaseDocument> unapprovedConsentOrders = new ArrayList<>();

        if (wrapper.getContestedConsentedApprovedOrders() != null
            && !wrapper.getContestedConsentedApprovedOrders().isEmpty()) {
            wrapper.getContestedConsentedApprovedOrders().forEach(order -> {
                approvedConsentOrders.add(order.getApprovedOrder().getConsentOrder());
            });
        }
        if (wrapper.getConsentedNotApprovedOrders() != null
            && !wrapper.getConsentedNotApprovedOrders().isEmpty()) {
            wrapper.getConsentedNotApprovedOrders().forEach(order -> {
                unapprovedConsentOrders.add(order.getApprovedOrder().getConsentOrder());
            });
        }
        List<CaseDocument> approvedConsentOrdersToShare = getMatchingSelectedOrders(selectedConsentOrders, approvedConsentOrders);
        List<CaseDocument> unapprovedConsentOrdersToShare = getMatchingSelectedOrders(selectedConsentOrders, unapprovedConsentOrders);

        shareAndPrintConsentOrdersWithApplicant(caseDetails, partyList, userAuthorisation, approvedConsentOrdersToShare, unapprovedConsentOrdersToShare);
        shareAndPrintConsentOrdersWithRespondent(caseDetails, partyList, userAuthorisation, approvedConsentOrdersToShare, unapprovedConsentOrdersToShare);
        shareAndPrintConsentOrdersWithIntervenerOne(caseDetails, partyList, userAuthorisation, approvedConsentOrdersToShare, unapprovedConsentOrdersToShare);
        shareAndPrintConsentOrdersWithIntervenerTwo(caseDetails, partyList, userAuthorisation, approvedConsentOrdersToShare, unapprovedConsentOrdersToShare);
        shareAndPrintConsentOrdersWithIntervenerThree(caseDetails, partyList, userAuthorisation, approvedConsentOrdersToShare, unapprovedConsentOrdersToShare);
        shareAndPrintConsentOrdersWithIntervenerFour(caseDetails, partyList, userAuthorisation, approvedConsentOrdersToShare, unapprovedConsentOrdersToShare);
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

    private void shareAndPrintConsentOrdersWithIntervenerFour(FinremCaseDetails caseDetails, List<String> partyList,
                                                              String userAuthorisation, List<CaseDocument> approvedConsentOrders,
                                                              List<CaseDocument> unapprovedConsentOrders) {
        Long caseId = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getCcdCode())) {
            log.info("Received request to send consent order to intervener4 for case {}", caseId);
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv4RefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrders.isEmpty()) {
                List<CaseDocument> approvedDocuments = addApprovedOrdersToCollection(caseData, approvedConsentOrders, approvedOrderColl);
                caseData.setIntv4OrderCollection(approvedOrderColl);
            }
            if (!unapprovedConsentOrders.isEmpty()) {
                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrders, refusedOrderColl);
                caseData.setIntv4RefusedOrderCollection(refusedOrderColl);
            }
            // log.info("Sending documents {} to intervener4 for case {}", documents, caseId);
            IntervenerWrapper wrapper = caseData.getIntervenerOneWrapper();
//            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
//                log.info("Sending consent order to intervener4 for case {}", caseId);
//                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
//                    userAuthorisation,
//                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
//            }
        }
    }

    private void shareAndPrintConsentOrdersWithIntervenerThree(FinremCaseDetails caseDetails, List<String> partyList,
                                                               String userAuthorisation, List<CaseDocument> approvedConsentOrders,
                                                               List<CaseDocument> unapprovedConsentOrders) {
        Long caseId = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getCcdCode())) {
            log.info("Received request to send consent order to intervener3 for case {}", caseId);
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv3RefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrders.isEmpty()) {
                List<CaseDocument> approvedDocuments = addApprovedOrdersToCollection(caseData, approvedConsentOrders, approvedOrderColl);
                caseData.setIntv3OrderCollection(approvedOrderColl);
            }
            if (!unapprovedConsentOrders.isEmpty()) {
                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrders, refusedOrderColl);
                caseData.setIntv3RefusedOrderCollection(refusedOrderColl);
            }
            // log.info("Sending documents {} to intervener3 for case {}", documents, caseId);
            IntervenerWrapper wrapper = caseData.getIntervenerThreeWrapper();
//            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
//                log.info("Sending consent order to intervener3 for case {}", caseId);
//                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
//                    userAuthorisation,
//                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
//            }
        }
    }

    private void shareAndPrintConsentOrdersWithIntervenerTwo(FinremCaseDetails caseDetails, List<String> partyList,
                                                             String userAuthorisation, List<CaseDocument> approvedConsentOrders,
                                                             List<CaseDocument> unapprovedConsentOrders) {
        Long caseId = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getCcdCode())) {
            log.info("Received request to send consent order to intervener2 for case {}", caseId);
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv2RefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrders.isEmpty()) {
                List<CaseDocument> approvedDocuments = addApprovedOrdersToCollection(caseData, approvedConsentOrders, approvedOrderColl);
                caseData.setIntv2OrderCollection(approvedOrderColl);
            }
            if (!unapprovedConsentOrders.isEmpty()) {
                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrders, refusedOrderColl);
                caseData.setIntv2RefusedOrderCollection(refusedOrderColl);
            }
            // log.info("Sending documents {} to intervener2 for case {}", documents, caseId);
            IntervenerWrapper wrapper = caseData.getIntervenerTwoWrapper();
//            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
//                log.info("Sending consent order to intervener2 for case {}", caseId);
//                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
//                    userAuthorisation,
//                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
//            }
        }
    }

    private void shareAndPrintConsentOrdersWithIntervenerOne(FinremCaseDetails caseDetails, List<String> partyList,
                                                             String userAuthorisation, List<CaseDocument> approvedConsentOrders,
                                                             List<CaseDocument> unapprovedConsentOrders) {
        Long caseId = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getCcdCode())) {
            log.info("Received request to send consent order to intervener1 for case {}", caseId);
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv1RefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrders.isEmpty()) {
                List<CaseDocument> approvedDocuments = addApprovedOrdersToCollection(caseData, approvedConsentOrders, approvedOrderColl);
                caseData.setIntv1OrderCollection(approvedOrderColl);
            }
            if (!unapprovedConsentOrders.isEmpty()) {
                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrders, refusedOrderColl);
                caseData.setIntv1RefusedOrderCollection(refusedOrderColl);
            }
            //log.info("Sending documents {} to intervener1 for case {}", documents, caseId);
            IntervenerWrapper wrapper = caseData.getIntervenerOneWrapper();
//            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
//                log.info("Sending consent order to intervener1 for case {}", caseId);
//                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
//                    userAuthorisation,
//                    documentHelper.getCaseDocumentsAsBulkPrintDocuments());
//            }
        }
    }

    private void shareAndPrintConsentOrdersWithRespondent(FinremCaseDetails caseDetails, List<String> partyList,
                                                          String userAuthorisation, List<CaseDocument> approvedConsentOrders,
                                                          List<CaseDocument> unapprovedConsentOrders) {
        Long caseId = caseDetails.getId();
        FinremCaseData caseData = caseDetails.getData();
        if (partyList.contains(CaseRole.RESP_SOLICITOR.getCcdCode())) {
            log.info("Received request to send consent order to respondent for case {}:", caseId);
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getRespOrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getRespRefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrders.isEmpty()) {
                List<CaseDocument> approvedDocuments = addApprovedOrdersToCollection(caseData, approvedConsentOrders, approvedOrderColl);
                caseData.setAppOrderCollection(approvedOrderColl);
            }
            if (!unapprovedConsentOrders.isEmpty()) {
                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrders, refusedOrderColl);
                caseData.setAppRefusedOrderCollection(refusedOrderColl);
            }
//            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
//                log.info("Sending consent order to respondent for case {}", caseId);
//                bulkPrintService.printRespondentDocuments(caseDetails,
//                    userAuthorisation,
//                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
//            }
        }
    }

    private void shareAndPrintConsentOrdersWithApplicant(FinremCaseDetails caseDetails,
                                                         List<String> partyList, String userAuthorisation,
                                                         List<CaseDocument> approvedConsentOrders,
                                                         List<CaseDocument> unapprovedConsentOrders) {
        FinremCaseData caseData = caseDetails.getData();
        Long caseId = caseDetails.getId();
        if (partyList.contains(CaseRole.APP_SOLICITOR.getCcdCode())) {
            log.info("Received request to send approved consent order to applicant for case {}:", caseId);
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getAppRefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrders.isEmpty()) {
                List<CaseDocument> approvedDocuments = addApprovedOrdersToCollection(caseData, approvedConsentOrders, approvedOrderColl);
                caseData.setAppOrderCollection(approvedOrderColl);
            }
            if (!unapprovedConsentOrders.isEmpty()) {
                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrders, refusedOrderColl);
                caseData.setAppRefusedOrderCollection(refusedOrderColl);
            }
//            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
//                log.info("Sending consent order to applicant for caseId {}", caseId);
//                bulkPrintService.printApplicantDocuments(caseDetails,
//                    userAuthorisation,
//                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
//            }
        }
    }

    private List<CaseDocument> addApprovedOrdersToCollection(FinremCaseData caseData,
                                                             List<CaseDocument> approvedConsentOrders,
                                                             List<ApprovedOrderCollection> approvedOrderColl) {
        List<CaseDocument> documentsToPrint = new ArrayList<>();
        approvedConsentOrders.forEach(order -> approvedOrderColl.add(getApprovedOrderCollection(order)));
        documentsToPrint.addAll(approvedConsentOrders);
        CaseDocument document = caseData.getAdditionalConsentInContestedDocument();
        if (document != null) {
            approvedOrderColl.add(getApprovedOrderCollection(document));
            documentsToPrint.add(document);
        }
        return documentsToPrint;
    }

    private List<CaseDocument> addUnapprovedOrdersToCollection(FinremCaseData caseData,
                                                               List<CaseDocument> unapprovedConsentOrders,
                                                               List<UnapprovedOrderCollection> unapprovedOrderColl) {
        List<CaseDocument> documentsToPrint = new ArrayList<>();
        unapprovedConsentOrders.forEach(order -> unapprovedOrderColl.add(getUnapprovedOrderCollection(order)));
        documentsToPrint.addAll(unapprovedConsentOrders);
        CaseDocument document = caseData.getAdditionalConsentInContestedDocument();
        if (document != null) {
            unapprovedOrderColl.add(getUnapprovedOrderCollection(document));
            documentsToPrint.add(document);
        }
        return documentsToPrint;
    }

    private ApprovedOrderCollection getApprovedOrderCollection(CaseDocument consentOrder) {
        return ApprovedOrderCollection.builder()
            .value(ApproveOrder.builder().caseDocument(consentOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private UnapprovedOrderCollection getUnapprovedOrderCollection(CaseDocument consentOrder) {
        return UnapprovedOrderCollection.builder()
            .value(UnapproveOrder.builder().caseDocument(consentOrder)
                .orderReceivedAt(LocalDateTime.now()).build()).build();
    }

    private List<CaseDocument> getMatchingSelectedOrders(DynamicMultiSelectList selectedDocs, List<CaseDocument> caseDocuments) {
        if (caseDocuments != null && !caseDocuments.isEmpty()) {
            return caseDocuments.stream().filter(doc ->
                selectedDocs.getValue().stream().anyMatch(e -> e.getLabel().equals(doc.getDocumentFilename()))).toList();
        }
        return new ArrayList<>();
    }
}
