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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapproveOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UnapprovedOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ConsentOrderWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderNotApprovedDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class SendConsentOrderInContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final NotificationService notificationService;
    private final ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService;
    private final ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService;
    private final CaseDataService caseDataService;


    public SendConsentOrderInContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                           GeneralOrderService generalOrderService,
                                                           GenericDocumentService genericDocumentService,
                                                           DocumentHelper documentHelper,
                                                           BulkPrintService bulkPrintService,
                                                           NotificationService notificationService,
                                                           CaseDataService caseDataService,
                                                           ConsentOrderApprovedDocumentService consentOrderApprovedDocumentService,
                                                           ConsentOrderNotApprovedDocumentService consentOrderNotApprovedDocumentService) {
        super(finremCaseDetailsMapper);
        this.generalOrderService = generalOrderService;
        this.genericDocumentService = genericDocumentService;
        this.documentHelper = documentHelper;
        this.bulkPrintService = bulkPrintService;
        this.notificationService = notificationService;
        this.caseDataService = caseDataService;
        this.consentOrderApprovedDocumentService = consentOrderApprovedDocumentService;
        this.consentOrderNotApprovedDocumentService = consentOrderNotApprovedDocumentService;
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
        List<CaseDocument> approvedConsentOrderDocumentsToShare = getMatchingSelectedApprovedOrders(selectedConsentOrders, approvedConsentOrders);
        List<CaseDocument> unapprovedConsentOrderDocumentsToShare = getMatchingSelectedUnapprovedOrders(selectedConsentOrders, unapprovedConsentOrders);

        shareAndPrintConsentOrdersWithApplicant(caseDetails, partyList, userAuthorisation, approvedConsentOrderDocumentsToShare, unapprovedConsentOrderDocumentsToShare, selectedConsentOrders);
        shareAndPrintConsentOrdersWithRespondent(caseDetails, partyList, userAuthorisation, approvedConsentOrderDocumentsToShare, unapprovedConsentOrderDocumentsToShare, selectedConsentOrders);
        shareAndPrintConsentOrdersWithIntervenerOne(caseDetails, partyList, userAuthorisation, approvedConsentOrderDocumentsToShare, unapprovedConsentOrderDocumentsToShare, selectedConsentOrders);
        shareAndPrintConsentOrdersWithIntervenerTwo(caseDetails, partyList, userAuthorisation, approvedConsentOrderDocumentsToShare, unapprovedConsentOrderDocumentsToShare, selectedConsentOrders);
        shareAndPrintConsentOrdersWithIntervenerThree(caseDetails, partyList, userAuthorisation, approvedConsentOrderDocumentsToShare, unapprovedConsentOrderDocumentsToShare, selectedConsentOrders);
        shareAndPrintConsentOrdersWithIntervenerFour(caseDetails, partyList, userAuthorisation, approvedConsentOrderDocumentsToShare, unapprovedConsentOrderDocumentsToShare, selectedConsentOrders);
        //TODO: Refactor with corresponders, add flags to send to relevant parties,
        // update tab fields (e.g. a is done in applicant method)
    }

    private void shareAndPrintConsentOrdersWithIntervenerFour(FinremCaseDetails caseDetails,
                                                              List<String> partyList, String userAuthorisation,
                                                              List<CaseDocument> approvedConsentOrderDocuments,
                                                              List<CaseDocument> unapprovedConsentOrderDocuments,
                                                              DynamicMultiSelectList selectedOrders) {
//        Long caseId = caseDetails.getId();
//        FinremCaseData caseData = caseDetails.getData();
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
//            log.info("Received request to send consent order to intervener4 for case {}", caseId);
//            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv4OrderCollection())
//                .orElse(new ArrayList<>());
//            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv4RefusedOrderCollection())
//                .orElse(new ArrayList<>());
//            if (!approvedConsentOrderDocuments.isEmpty()) {
//                //List<CaseDocument> approvedDocuments = addAdditionalDocumentToCollection(caseData, approvedConsentOrderDocuments, approvedOrderColl);
//                caseData.setIntv4OrderCollection(approvedOrderColl);
//            }
//            if (!unapprovedConsentOrderDocuments.isEmpty()) {
//                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrderDocuments, refusedOrderColl);
//                caseData.setIntv4RefusedOrderCollection(refusedOrderColl);
//            }
//            // log.info("Sending documents {} to intervener4 for case {}", documents, caseId);
//            IntervenerWrapper wrapper = caseData.getIntervenerOneWrapper();
////            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
////                log.info("Sending consent order to intervener4 for case {}", caseId);
////                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
////                    userAuthorisation,
////                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
////            }
//        }
    }

    private void shareAndPrintConsentOrdersWithIntervenerThree(FinremCaseDetails caseDetails,
                                                               List<String> partyList, String userAuthorisation,
                                                               List<CaseDocument> approvedConsentOrderDocuments,
                                                               List<CaseDocument> unapprovedConsentOrderDocuments,
                                                               DynamicMultiSelectList selectedOrders) {
//        Long caseId = caseDetails.getId();
//        FinremCaseData caseData = caseDetails.getData();
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
//            log.info("Received request to send consent order to intervener3 for case {}", caseId);
//            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv3OrderCollection())
//                .orElse(new ArrayList<>());
//            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv3RefusedOrderCollection())
//                .orElse(new ArrayList<>());
//            if (!approvedConsentOrderDocuments.isEmpty()) {
//                //List<CaseDocument> approvedDocuments = addAdditionalDocumentToCollection(caseData, approvedConsentOrderDocuments, approvedOrderColl);
//                caseData.setIntv3OrderCollection(approvedOrderColl);
//            }
//            if (!unapprovedConsentOrderDocuments.isEmpty()) {
//                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrderDocuments, refusedOrderColl);
//                caseData.setIntv3RefusedOrderCollection(refusedOrderColl);
//            }
//            // log.info("Sending documents {} to intervener3 for case {}", documents, caseId);
//            IntervenerWrapper wrapper = caseData.getIntervenerThreeWrapper();
////            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
////                log.info("Sending consent order to intervener3 for case {}", caseId);
////                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
////                    userAuthorisation,
////                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
////            }
//        }
    }

    private void shareAndPrintConsentOrdersWithIntervenerTwo(FinremCaseDetails caseDetails,
                                                             List<String> partyList, String userAuthorisation,
                                                             List<CaseDocument> approvedConsentOrderDocuments,
                                                             List<CaseDocument> unapprovedConsentOrderDocuments,
                                                             DynamicMultiSelectList selectedOrders) {
//        Long caseId = caseDetails.getId();
//        FinremCaseData caseData = caseDetails.getData();
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
//            log.info("Received request to send consent order to intervener2 for case {}", caseId);
//            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv2OrderCollection())
//                .orElse(new ArrayList<>());
//            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv2RefusedOrderCollection())
//                .orElse(new ArrayList<>());
//            if (!approvedConsentOrderDocuments.isEmpty()) {
//                //List<CaseDocument> approvedDocuments = addAdditionalDocumentToCollection(caseData, approvedConsentOrderDocuments, approvedOrderColl);
//                caseData.setIntv2OrderCollection(approvedOrderColl);
//            }
//            if (!unapprovedConsentOrderDocuments.isEmpty()) {
//                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrderDocuments, refusedOrderColl);
//                caseData.setIntv2RefusedOrderCollection(refusedOrderColl);
//            }
//            // log.info("Sending documents {} to intervener2 for case {}", documents, caseId);
//            IntervenerWrapper wrapper = caseData.getIntervenerTwoWrapper();
////            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
////                log.info("Sending consent order to intervener2 for case {}", caseId);
////                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
////                    userAuthorisation,
////                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
////            }
//        }
    }

    private void shareAndPrintConsentOrdersWithIntervenerOne(FinremCaseDetails caseDetails,
                                                             List<String> partyList, String userAuthorisation,
                                                             List<CaseDocument> approvedConsentOrderDocuments,
                                                             List<CaseDocument> unapprovedConsentOrderDocuments,
                                                             DynamicMultiSelectList selectedOrders) {
//        Long caseId = caseDetails.getId();
//        FinremCaseData caseData = caseDetails.getData();
//        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
//            log.info("Received request to send consent order to intervener1 for case {}", caseId);
//            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getIntv1OrderCollection())
//                .orElse(new ArrayList<>());
//            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getIntv1RefusedOrderCollection())
//                .orElse(new ArrayList<>());
//            if (!approvedConsentOrderDocuments.isEmpty()) {
//                //List<CaseDocument> approvedDocuments = addAdditionalDocumentToCollection(caseData, approvedConsentOrderDocuments, approvedOrderColl);
//                caseData.setIntv1OrderCollection(approvedOrderColl);
//            }
//            if (!unapprovedConsentOrderDocuments.isEmpty()) {
//                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrderDocuments, refusedOrderColl);
//                caseData.setIntv1RefusedOrderCollection(refusedOrderColl);
//            }
//            //log.info("Sending documents {} to intervener1 for case {}", documents, caseId);
//            IntervenerWrapper wrapper = caseData.getIntervenerOneWrapper();
////            if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(wrapper, caseDetails)) {
////                log.info("Sending consent order to intervener1 for case {}", caseId);
////                bulkPrintService.printIntervenerDocuments(wrapper, caseDetails,
////                    userAuthorisation,
////                    documentHelper.getCaseDocumentsAsBulkPrintDocuments());
////            }
//        }
    }

    private void shareAndPrintConsentOrdersWithRespondent(FinremCaseDetails caseDetails,
                                                          List<String> partyList, String userAuthorisation,
                                                          List<CaseDocument> approvedConsentOrderDocuments,
                                                          List<CaseDocument> unapprovedConsentOrderDocuments,
                                                          DynamicMultiSelectList selectedOrders) {
//        Long caseId = caseDetails.getId();
//        FinremCaseData caseData = caseDetails.getData();
//        if (partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
//            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getRespOrderCollection())
//                .orElse(new ArrayList<>());
//            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getRespRefusedOrderCollection())
//                .orElse(new ArrayList<>());
//            if (!approvedConsentOrderDocuments.isEmpty()) {
//                log.info("Received request to send approved consent order pack to respondent for case {}:", caseId);
//
//                //List<CaseDocument> approvedDocuments = addAdditionalDocumentToCollection(caseData, approvedConsentOrderDocuments, approvedOrderColl);
//                caseData.setAppOrderCollection(approvedOrderColl);
//            }
//            if (!unapprovedConsentOrderDocuments.isEmpty()) {
//                List<CaseDocument> unapprovedDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrderDocuments, refusedOrderColl);
//                caseData.setAppRefusedOrderCollection(refusedOrderColl);
//            }
////            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
////                log.info("Sending consent order to respondent for case {}", caseId);
////                bulkPrintService.printRespondentDocuments(caseDetails,
////                    userAuthorisation,
////                    documentHelper.getCaseDocumentsAsBulkPrintDocuments(documents));
////            }
//        }
    }

    private void shareAndPrintConsentOrdersWithApplicant(FinremCaseDetails caseDetails,
                                                         List<String> partyList, String userAuthorisation,
                                                         List<CaseDocument> approvedConsentOrderDocuments,
                                                         List<CaseDocument> unapprovedConsentOrderDocuments,
                                                         DynamicMultiSelectList selectedOrders) {
        FinremCaseData caseData = caseDetails.getData();
        Long caseId = caseDetails.getId();
        if (partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
            List<ApprovedOrderCollection> approvedOrderColl = Optional.ofNullable(caseData.getAppOrderCollection())
                .orElse(new ArrayList<>());
            List<UnapprovedOrderCollection> refusedOrderColl = Optional.ofNullable(caseData.getAppRefusedOrderCollection())
                .orElse(new ArrayList<>());
            if (!approvedConsentOrderDocuments.isEmpty()) {
                log.info("Received request to send approved consent order pack to applicant for case {}:", caseId);
                List<CaseDocument> approvedCaseDocuments = new ArrayList<>();
                List<CaseDocument> approvedLetterDocuments = createApprovedOrderLetterPackForApplicant(caseDetails, userAuthorisation, approvedCaseDocuments, selectedOrders);
                List<BulkPrintDocument> approvedBulkPrintPack = new ArrayList<>();
                approvedLetterDocuments.forEach(doc -> approvedBulkPrintPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(doc)));
                approvedLetterDocuments.forEach(document -> approvedOrderColl.add(getApprovedOrderCollection(document)));
                caseData.setAppOrderCollection(approvedOrderColl);
                if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                    log.info("Sending approved order pack to applicant via bulk print for case {}", caseDetails.getId());
                    UUID applicantLetterId = bulkPrintService.printApplicantDocuments(
                        caseDetails, userAuthorisation, approvedBulkPrintPack);
                    caseData.setBulkPrintLetterIdApp(applicantLetterId.toString());
                }
            }
            if (!unapprovedConsentOrderDocuments.isEmpty()) {
                log.info("Received request to send refused consent order to applicant for case {}:", caseId);
                List<CaseDocument> unapprovedCaseDocuments = addUnapprovedOrdersToCollection(caseData, unapprovedConsentOrderDocuments, refusedOrderColl);
                BulkPrintDocument unapprovedDocument = consentOrderNotApprovedDocumentService.coverLetter(caseDetails, userAuthorisation);
                List<BulkPrintDocument> unapprovedBulkPrintPack = new ArrayList<>(documentHelper.getCaseDocumentsAsBulkPrintDocuments(unapprovedCaseDocuments));
                unapprovedBulkPrintPack.add(unapprovedDocument);
                caseData.setAppRefusedOrderCollection(refusedOrderColl);
                if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
                    log.info("Sending refused order to applicant via bulk print for case {}", caseDetails.getId());
                    //this will never be a general order since the precondition state must be consentedOrderApproved or consentedOrderNotApproved
                    UUID applicantLetterId = bulkPrintService.printApplicantDocuments(
                        caseDetails, userAuthorisation, unapprovedBulkPrintPack);
                    caseData.setBulkPrintLetterIdApp(applicantLetterId.toString());
                }
            }
        }
    }

    private List<CaseDocument> createApprovedOrderLetterPackForApplicant(FinremCaseDetails caseDetails, String userAuthorisation, List<CaseDocument> documents, DynamicMultiSelectList selectedOrders) {
        FinremCaseData caseData = caseDetails.getData();
        log.info("Preparing approved consent order letter pack to be sent for Bulk Print, case {}", caseDetails.getId());
        List<CaseDocument> caseDocuments = new ArrayList<>();

        if (caseDataService.isPaperApplication(caseData)) {
            CaseDocument coverLetter = consentOrderApprovedDocumentService.generateApprovedConsentOrderCoverLetter(caseDetails, userAuthorisation);
            caseDocuments.add(coverLetter);
        }

        List<ConsentOrderCollection> approvedOrderList = consentOrderApprovedDocumentService.covert(
            caseData.getConsentOrderWrapper().getContestedConsentedApprovedOrders());

        List<ConsentOrderCollection> selectedApprovedOrderList = approvedOrderList.stream().filter(order ->
            selectedOrders.getValue().stream().anyMatch(e -> e.getLabel().equals(
                "[Approved Order]" + " - " + order.getApprovedOrder().getConsentOrder().getDocumentFilename()))).toList();

        List<ConsentOrderCollection> convertedData = new ArrayList<>();
        if (!selectedApprovedOrderList.isEmpty()) {
            selectedApprovedOrderList.forEach(order ->
                consentOrderApprovedDocumentService.generateConsentInContestedBulkPrintDocuments(order,
                caseData, documents, convertedData,
                userAuthorisation, caseDetails.getId().toString()));
        }

        caseDocuments.addAll(documents);

        return caseDocuments;
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

    private List<CaseDocument> getMatchingSelectedApprovedOrders(DynamicMultiSelectList selectedDocs, List<CaseDocument> caseDocuments) {
        if (caseDocuments != null && !caseDocuments.isEmpty()) {
            return caseDocuments.stream().filter(doc ->
                selectedDocs.getValue().stream().anyMatch(e -> (e.getLabel()).equals("[Approved Order]" + " - " + doc.getDocumentFilename()))).toList();
        }
        return new ArrayList<>();
    }

    private List<CaseDocument> getMatchingSelectedUnapprovedOrders(DynamicMultiSelectList selectedDocs, List<CaseDocument> caseDocuments) {
        if (caseDocuments != null && !caseDocuments.isEmpty()) {
            return caseDocuments.stream().filter(doc ->
                selectedDocs.getValue().stream().anyMatch(e -> (e.getLabel()).equals("[Refused Order]" + " - " + doc.getDocumentFilename()))).toList();
        }
        return new ArrayList<>();
    }
}
