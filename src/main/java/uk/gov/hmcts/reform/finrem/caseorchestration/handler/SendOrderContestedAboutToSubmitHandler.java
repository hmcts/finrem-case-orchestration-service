package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidCaseDataException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralOrderService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_ORDER_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_ORDER_APPROVED_COVER_LETTER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ADDITIONAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_ORDER_LATEST_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV1_ADDITIONAL_DOC_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV1_FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV2_ADDITIONAL_DOC_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV2_FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV3_ADDITIONAL_DOC_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV3_FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV4_ADDITIONAL_DOC_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTV4_FINAL_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_LIST;


@Slf4j
@Service
@RequiredArgsConstructor
public class SendOrderContestedAboutToSubmitHandler
    implements CallbackHandler<Map<String, Object>> {

    private final BulkPrintService bulkPrintService;
    private final GeneralOrderService generalOrderService;
    private final GenericDocumentService genericDocumentService;
    private final NotificationService notificationService;
    private final CaseDataService caseDataService;
    private final DocumentHelper documentHelper;

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.SEND_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        try {
            List<String> partyList = generalOrderService.getPartyList(caseDetails);
            printAndMailGeneralOrderToParties(caseDetails, partyList, userAuthorisation);
            List<CaseDocument> hearingOrders = heardingOrderToProcess(caseDetails);
            printAndMailHearingDocuments(caseDetails, hearingOrders, partyList, userAuthorisation);
            stampFinalOrder(caseDetails, hearingOrders, partyList, userAuthorisation);
        } catch (InvalidCaseDataException e) {
            return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().errors(List.of(e.getMessage())).build();
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(caseDetails.getData()).build();
    }

    private List<CaseDocument> heardingOrderToProcess(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        List<CaseDocument> orders = new ArrayList<>();
        List<HearingOrderCollectionData> hearingOrders = documentHelper.getHearingOrderDocuments(caseData);

        DynamicMultiSelectList selectedDocs = generalOrderService.getList(caseData.get(ORDER_LIST));
        if (selectedDocs != null) {
            List<DynamicMultiSelectListElement> docs = selectedDocs.getValue();
            docs.forEach(doc -> hearingOrders.forEach(obj -> addToList(doc, obj, orders, caseDetails.getId())));
        }
        return orders;
    }

    private void addToList(DynamicMultiSelectListElement doc, HearingOrderCollectionData obj,
                           List<CaseDocument> hearingDocumentPack, Long caseId) {
        if (obj.getId().equals(doc.getCode())) {
            CaseDocument caseDocument = obj.getHearingOrderDocuments().getUploadDraftDocument();
            log.info("Adding document to pack {} for caseId {}", caseDocument, caseId);
            hearingDocumentPack.add(caseDocument);
        }
    }

    private void stampFinalOrder(CaseDetails caseDetails, List<CaseDocument> hearingOrders, List<String> partyList, String authToken) {
        Long caseId = caseDetails.getId();
        hearingOrders.forEach(orderToStamp -> {
            log.info("Received request to stampFinalOrder called with Case ID = {},"
                    + " latestHearingOrder = {}", caseId, orderToStamp);
            stampAndAddToCollection(caseDetails, orderToStamp, partyList, authToken);
        });
    }

    private void stampAndAddToCollection(CaseDetails caseDetails, CaseDocument latestHearingOrder,
                                         List<String> partyList, String authToken) {
        Long caseId =  caseDetails.getId();
        Map<String, Object> caseData = caseDetails.getData();

        StampType stampType = documentHelper.getStampType(caseData);
        CaseDocument stampedDocs = genericDocumentService.stampDocument(latestHearingOrder, authToken, stampType);
        log.info("Stamped Documents = {} for caseId {}", stampedDocs, caseId);

        List<HearingOrderCollectionData> finalOrderCollection = Optional.ofNullable(documentHelper.getFinalOrderDocuments(caseData))
            .orElse(new ArrayList<>());

        finalOrderCollection.add(prepareFinalOrderList(stampedDocs));
        log.info("Existing final order collection = {}", finalOrderCollection);

        caseData.put(FINAL_ORDER_COLLECTION, finalOrderCollection);
        log.info("Finished stamping final order for caseId {}", caseId);



        CaseDocument obj = documentHelper.convertToCaseDocumentIfObjNotNull(caseData.get(ADDITIONAL_ORDER_DOC));
        if (obj != null) {
            List<HearingOrderCollectionData> additionalOrderCollection = Optional.ofNullable(documentHelper.getFinalAdditionalDocOrderDocuments(caseData))
                .orElse(new ArrayList<>());
            additionalOrderCollection.add(prepareFinalOrderList(obj));
            caseData.put(FINAL_ADDITIONAL_ORDER_COLLECTION, additionalOrderCollection);
        }


        if (partyList.contains(CaseRole.INTVR_SOLICITOR_1.getValue())) {
            List<IntervenerOrderCollection> intv1FinalOrder
                = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV1_FINAL_ORDER_COLLECTION))
                .orElse(new ArrayList<>());
            intv1FinalOrder.add(prepareIntvFinalOrderList(stampedDocs));
            caseData.put(INTV1_FINAL_ORDER_COLLECTION,intv1FinalOrder);
            if (obj != null) {
                List<IntervenerOrderCollection> intv1AdditionalOrderDoc
                    = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV1_ADDITIONAL_DOC_ORDER_COLLECTION))
                    .orElse(new ArrayList<>());
                intv1AdditionalOrderDoc.add(prepareIntvFinalOrderList(obj));
                caseData.put(INTV1_ADDITIONAL_DOC_ORDER_COLLECTION, intv1AdditionalOrderDoc);
            }
        }
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_2.getValue())) {
            List<IntervenerOrderCollection> intv2FinalOrder
                = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV2_FINAL_ORDER_COLLECTION))
                .orElse(new ArrayList<>());
            intv2FinalOrder.add(prepareIntvFinalOrderList(stampedDocs));
            caseData.put(INTV2_FINAL_ORDER_COLLECTION,intv2FinalOrder);
            if (obj != null) {
                List<IntervenerOrderCollection> intv2AdditionalOrderDoc
                    = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV2_ADDITIONAL_DOC_ORDER_COLLECTION))
                    .orElse(new ArrayList<>());
                intv2AdditionalOrderDoc.add(prepareIntvFinalOrderList(obj));
                caseData.put(INTV2_ADDITIONAL_DOC_ORDER_COLLECTION, intv2AdditionalOrderDoc);
            }
        }
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_3.getValue())) {
            List<IntervenerOrderCollection> intv3FinalOrder
                = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV3_FINAL_ORDER_COLLECTION))
                .orElse(new ArrayList<>());
            intv3FinalOrder.add(prepareIntvFinalOrderList(stampedDocs));
            caseData.put(INTV3_FINAL_ORDER_COLLECTION,intv3FinalOrder);
            if (obj != null) {
                List<IntervenerOrderCollection> intv3AdditionalOrderDoc
                    = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV3_ADDITIONAL_DOC_ORDER_COLLECTION))
                    .orElse(new ArrayList<>());
                intv3AdditionalOrderDoc.add(prepareIntvFinalOrderList(obj));
                caseData.put(INTV3_ADDITIONAL_DOC_ORDER_COLLECTION, intv3AdditionalOrderDoc);
            }
        }
        if (partyList.contains(CaseRole.INTVR_SOLICITOR_4.getValue())) {
            List<IntervenerOrderCollection> intv4FinalOrder
                = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV4_FINAL_ORDER_COLLECTION))
                .orElse(new ArrayList<>());
            intv4FinalOrder.add(prepareIntvFinalOrderList(stampedDocs));
            caseData.put(INTV4_FINAL_ORDER_COLLECTION,intv4FinalOrder);
            if (obj != null) {
                List<IntervenerOrderCollection> intv4AdditionalOrderDoc
                    = Optional.ofNullable(documentHelper.getIntervenerFinalOrderDocuments(caseData, INTV4_ADDITIONAL_DOC_ORDER_COLLECTION))
                    .orElse(new ArrayList<>());
                intv4AdditionalOrderDoc.add(prepareIntvFinalOrderList(obj));
                caseData.put(INTV4_ADDITIONAL_DOC_ORDER_COLLECTION, intv4AdditionalOrderDoc);
            }
        }
        caseData.remove(ADDITIONAL_ORDER_DOC);
    }

    private HearingOrderCollectionData prepareFinalOrderList(CaseDocument stampedDocs) {
        return HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument
                .builder().uploadDraftDocument(stampedDocs).build())
            .build();
    }

    private IntervenerOrderCollection prepareIntvFinalOrderList(CaseDocument stampedDocs) {
        return IntervenerOrderCollection.builder().value(IntervenerOrder.builder().approveOrder(stampedDocs).build())
            .build();
    }

    private void printAndMailGeneralOrderToParties(CaseDetails caseDetails, List<String> partyList, String authorisationToken) {
        if (contestedGeneralOrderPresent(caseDetails)) {
            BulkPrintDocument generalOrder = generalOrderService.getLatestGeneralOrderAsBulkPrintDocument(caseDetails.getData(), authorisationToken);

            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
                && partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }

            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
                && partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, singletonList(generalOrder));
            }
        }
    }

    private void printAndMailHearingDocuments(CaseDetails caseDetails, List<CaseDocument> hearingOrders, List<String> partyList,
                                              String authorisationToken) {
        if (caseDataService.isContestedApplication(caseDetails)) {
            List<BulkPrintDocument> hearingDocumentPack = createHearingDocumentPack(caseDetails, hearingOrders, authorisationToken);

            if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
                && partyList.contains(CaseRole.APP_SOLICITOR.getValue())) {
                log.info("Received request to send hearing pack for applicant for case {}:", caseDetails.getId());
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }

            if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
                && partyList.contains(CaseRole.RESP_SOLICITOR.getValue())) {
                log.info("Received request to send hearing pack for respondent for case {}:", caseDetails.getId());
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, hearingDocumentPack);
            }
        }
    }

    private List<BulkPrintDocument> createHearingDocumentPack(CaseDetails caseDetails, List<CaseDocument> hearingOrders, String authorisationToken) {

        Long id = caseDetails.getId();
        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> hearingDocumentPack = new ArrayList<>();

        documentHelper.getDocumentLinkAsBulkPrintDocument(caseData, CONTESTED_ORDER_APPROVED_COVER_LETTER).ifPresent(hearingDocumentPack::add);
        hearingOrders.forEach(order -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(order)));

        CaseDocument obj = documentHelper.convertToCaseDocumentIfObjNotNull(caseData.get(ADDITIONAL_ORDER_DOC));
        if (obj != null) {
            CaseDocument caseDocument = genericDocumentService
                .convertDocumentIfNotPdfAlready(obj, authorisationToken);
            log.info("additional uploaded document with send order {} for caseId {}", caseDocument, id);
            hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument));
            caseData.put(ADDITIONAL_ORDER_DOC, caseDocument);
        }

        if (documentHelper.hasAnotherHearing(caseData)) {
            Optional<CaseDocument> latestAdditionalHearingDocument = documentHelper.getLatestAdditionalHearingDocument(caseData);
            latestAdditionalHearingDocument.ifPresent(
                caseDocument -> hearingDocumentPack.add(documentHelper.getCaseDocumentAsBulkPrintDocument(caseDocument)));
        }

        List<BulkPrintDocument> otherHearingDocuments = documentHelper.getHearingDocumentsAsBulkPrintDocuments(
            caseData, authorisationToken);

        if (otherHearingDocuments != null) {
            hearingDocumentPack.addAll(otherHearingDocuments);
        }
        return hearingDocumentPack;
    }

    private boolean contestedGeneralOrderPresent(CaseDetails caseDetails) {
        return !isNull(caseDetails.getData().get(GENERAL_ORDER_LATEST_DOCUMENT));
    }
}
