package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.FinremAdditionalHearingCorresponder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdditionalHearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final BulkPrintService bulkPrintService;
    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final FinremAdditionalHearingCorresponder finremAdditionalHearingCorresponder;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final OrderDateService orderDateService;
    private static final String ADDITIONAL_MESSAGE = "Additional hearing document not required for Case ID: {}";

    public void createAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails) throws JsonProcessingException {
        Map<String, Object> caseData = caseDetails.getData();
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
        Map<String, Object> courtDetails = (Map<String, Object>)
            courtDetailsMap.get(caseData.get(CaseHearingFunctions.getSelectedHearingCourt(caseData)));
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);

        prepareHearingCaseDetails(caseDetailsCopy, courtDetails, caseData.get(HEARING_TYPE),
            caseData.get(HEARING_DATE),
            caseData.get(HEARING_TIME), caseData.get(TIME_ESTIMATE));
        caseDetailsCopy.getData().put("AnyOtherDirections", caseData.get(HEARING_ADDITIONAL_INFO));

        CaseDocument document = generateAdditionalHearingDocument(caseDetailsCopy, authorisationToken);
        addAdditionalHearingDocumentToCaseData(caseDetails, document);
    }

    public void sendAdditionalHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        finremAdditionalHearingCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }

    public List<DirectionOrderCollection> getApprovedHearingOrders(FinremCaseDetails caseDetails, String authorisationToken) {
        List<DirectionOrderCollection> uploadHearingOrder = caseDetails.getData().getUploadHearingOrder();
        return orderDateService.syncCreatedDateAndMarkDocumentNotStamped(uploadHearingOrder, authorisationToken);
    }

    public void sortDirectionDetailsCollection(FinremCaseData caseData) {
        List<DirectionDetailCollection> directionDetailsCollection
            = Optional.ofNullable(caseData.getDirectionDetailsCollection()).orElse(new ArrayList<>());
        if (!directionDetailsCollection.isEmpty()) {
            directionDetailsCollection.sort(Comparator.comparing(
                DirectionDetailCollection::getValue, Comparator.comparing(
                    DirectionDetail::getDateOfHearing, Comparator.nullsLast(
                        Comparator.reverseOrder()
                    )
                )
            ));
        }
    }

    private DirectionOrderCollection getDirectionOrderCollection(DirectionOrder originalDirectionOrder,
                                                                 CaseDocument caseDocument, LocalDateTime orderDateTime) {
        return DirectionOrderCollection.builder().value(originalDirectionOrder.toBuilder()
            .uploadDraftDocument(caseDocument)
            .orderDateTime(orderDateTime)
            .isOrderStamped(YesOrNo.YES)
            .build()).build();
    }

    /**
     * Stamps and collects orders from unprocessedUploadHearingDocuments, updating uploadHearingOrder and finalOrderCollection.
     *
     * @param caseDetails the case details containing the orders
     * @param authorisationToken the authorisation token for document stamping
     */
    public void stampAndCollectOrderCollection(FinremCaseDetails caseDetails, String authorisationToken) {
        String caseId = caseDetails.getId().toString();
        log.info("Processing unprocessed upload hearing documents for Case ID: {}", caseId);
        FinremCaseData caseData = caseDetails.getData();

        List<DirectionOrderCollection> finalOrderCollection = orderDateService
            .syncCreatedDateAndMarkDocumentStamped(caseData.getFinalOrderCollection(), authorisationToken);
        List<DirectionOrderCollection> newFinalOrderCollection = new ArrayList<>(emptyIfNull(caseData.getFinalOrderCollection()));

        List<DirectionOrderCollection> unprocessedOrders = Optional.ofNullable(caseData.getUnprocessedUploadHearingDocuments())
            .orElse(List.of());

        if (!unprocessedOrders.isEmpty()) {
            List<DirectionOrderCollection> updatedUploadHearingOrder = new ArrayList<>(emptyIfNull(caseData.getUploadHearingOrder()));

            for (DirectionOrderCollection unprocessedOrder : unprocessedOrders) {
                CaseDocument uploadDraftDocument = unprocessedOrder.getValue().getUploadDraftDocument();
                LocalDateTime orderDateTime = unprocessedOrder.getValue().getOrderDateTime();

                CaseDocument stampedDocument = getStampedDocs(authorisationToken, caseData, caseId, uploadDraftDocument);
                log.info("Stamped document {} for Case ID: {}", stampedDocument.getDocumentFilename(), caseId);

                // Replace or add the stamped order in uploadHearingOrder
                replaceOrAddOrder(updatedUploadHearingOrder, uploadDraftDocument, stampedDocument, orderDateTime);

                // Add to finalOrderCollection if not already present
                if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, uploadDraftDocument)) {
                    newFinalOrderCollection.add(documentHelper.prepareFinalOrder(stampedDocument));
                }
            }

            caseData.setUploadHearingOrder(updatedUploadHearingOrder);
            caseData.setFinalOrderCollection(newFinalOrderCollection);
            if (!updatedUploadHearingOrder.isEmpty()) {
                caseData.setLatestDraftHearingOrder(updatedUploadHearingOrder.getLast().getValue().getUploadDraftDocument());
            }
        }
    }

    /**
     * Replaces an order in the collection if it exists, otherwise adds it.
     *
     * @param orders the list of orders to update
     * @param originalDoc the original document to match
     * @param stampedDoc the new stamped document
     * @param orderDateTime the order date time
     */
    private void replaceOrAddOrder(List<DirectionOrderCollection> orders, CaseDocument originalDoc, CaseDocument stampedDoc, LocalDateTime orderDateTime) {
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getValue().getUploadDraftDocument().getDocumentUrl().equals(originalDoc.getDocumentUrl())) {
                orders.set(i, getDirectionOrderCollection(orders.get(i).getValue(), stampedDoc, orderDateTime));
                return;
            }
        }
        // If not found, add as new
        orders.add(DirectionOrderCollection.builder()
            .value(DirectionOrder.builder()
                .uploadDraftDocument(stampedDoc)
                .orderDateTime(orderDateTime)
                .isOrderStamped(YesOrNo.YES)
                .build())
            .build());
    }

    public void storeHearingNotice(FinremCaseDetails caseDetails, String authorisationToken)
        throws CourtDetailsParseException, JsonProcessingException {

        FinremCaseData caseData = caseDetails.getData();
        String caseId = String.valueOf(caseDetails.getId());

        List<DirectionDetailCollection> directionDetailsCollection = emptyIfNull(caseData.getDirectionDetailsCollection());

        // check that the list contains one or more values for the court hearing information
        if (!directionDetailsCollection.isEmpty()) {
            DirectionDetail directionDetail = directionDetailsCollection.getLast().getValue();

            // if the latest court hearing has specified another hearing as No, don't create an additional hearing document
            if (NO_VALUE.equalsIgnoreCase(nullToEmpty(directionDetail.getIsAnotherHearingYN()))) {
                log.info(ADDITIONAL_MESSAGE, caseId);
                return;
            }

            Map<String, Object> localCourtFieldSelections = convertToMap(directionDetail.getLocalCourt());
            log.info("Case ID: {} localCourtFieldSelections: {}", caseId, localCourtFieldSelections.size());
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);

            String selectedCourtListId = CaseHearingFunctions.getSelectedCourtComplexType(localCourtFieldSelections);
            log.info("Case ID: {} selectedCourtListId: {}", caseId, selectedCourtListId);
            Object selectedLocalCourt = localCourtFieldSelections.get(selectedCourtListId);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(selectedLocalCourt);

            CaseDetails mapToCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
            CaseDetails caseDetailsCopy = documentHelper.deepCopy(mapToCaseDetails, CaseDetails.class);
            prepareHearingCaseDetails(caseDetailsCopy, courtDetails,
                directionDetail.getTypeOfHearing(),
                directionDetail.getDateOfHearing().toString(),
                directionDetail.getHearingTime(),
                directionDetail.getTimeEstimate());

            CaseDocument document = generateAdditionalHearingDocument(caseDetailsCopy, authorisationToken);
            addAdditionalHearingDocumentToCaseData(caseDetails, document);
            sortDirectionDetailsCollection(caseData);
        }
    }

    public Map<String, Object> convertToMap(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private CaseDocument generateAdditionalHearingDocument(CaseDetails caseDetailsCopy, String authorisationToken) {
        log.info("Generating Additional Hearing Document for Case ID: {}", caseDetailsCopy.getId());

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
    }

    private void prepareHearingCaseDetails(CaseDetails caseDetails, Map<String, Object> courtDetails,
                                           Object hearingType, Object hearingDate, Object hearingTime, Object hearingLength) {
        Map<String, Object> caseData = caseDetails.getData();

        CourtDetailsTemplateFields selectedFRCDetails = CourtDetailsTemplateFields.builder()
            .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
            .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
            .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
            .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
            .build();

        caseData.put("HearingType", hearingType);
        caseData.put("HearingVenue", selectedFRCDetails.getCourtName());
        caseData.put("HearingDate", hearingDate);
        caseData.put("HearingTime", hearingTime);
        caseData.put("HearingLength", hearingLength);
        caseData.put("AdditionalHearingDated", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()));

        caseData.put("CourtName", selectedFRCDetails.getCourtName());
        caseData.put("CourtAddress", selectedFRCDetails.getCourtAddress());
        caseData.put("CourtPhone", selectedFRCDetails.getPhoneNumber());
        caseData.put("CourtEmail", selectedFRCDetails.getEmail());

        caseData.put("CCDCaseNumber", caseDetails.getId());
        caseData.put("DivorceCaseNumber", caseData.get(DIVORCE_CASE_NUMBER));
        caseData.put("ApplicantName", caseDataService.buildFullApplicantName(caseDetails));
        caseData.put("RespondentName", caseDataService.buildFullRespondentName(caseDetails));
    }

    protected void addAdditionalHearingDocumentToCaseData(FinremCaseDetails caseDetails, CaseDocument document) {
        AdditionalHearingDocumentCollection generatedDocumentData = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder()
                .document(document)
                .additionalHearingDocumentDate(LocalDateTime.now())
                .build())
            .build();

        FinremCaseData data = caseDetails.getData();
        List<AdditionalHearingDocumentCollection> additionalHearingDocumentCollections = Optional.ofNullable(
            data.getListForHearingWrapper().getAdditionalHearingDocuments())
            .orElse(new ArrayList<>(1));

        additionalHearingDocumentCollections.add(generatedDocumentData);

        data.getListForHearingWrapper().setAdditionalHearingDocuments(additionalHearingDocumentCollections);
    }

    protected void addAdditionalHearingDocumentToCaseData(CaseDetails caseDetails, CaseDocument document) {
        AdditionalHearingDocumentData generatedDocumentData = AdditionalHearingDocumentData.builder()
            .additionalHearingDocument(AdditionalHearingDocument.builder()
                .document(document)
                .additionalHearingDocumentDate(LocalDateTime.now())
                .build())
            .build();

        Map<String, Object> caseData = caseDetails.getData();
        List<AdditionalHearingDocumentData> additionalHearingDocumentDataList =
            Optional.ofNullable(caseData.get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION))
                .map(documentHelper::convertToAdditionalHearingDocumentData)
                .orElse(new ArrayList<>(1));

        additionalHearingDocumentDataList.add(generatedDocumentData);

        caseData.put(ADDITIONAL_HEARING_DOCUMENT_COLLECTION, additionalHearingDocumentDataList);
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void bulkPrintAdditionalHearingDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<AdditionalHearingDocumentData> additionalHearingDocumentData =
            documentHelper.convertToAdditionalHearingDocumentData(
                caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION));

        List<BulkPrintDocument> document = new ArrayList<>();
        if (caseDetails.getData().get(HEARING_ADDITIONAL_DOC) != null) {
            BulkPrintDocument additionalUploadedDoc
                = documentHelper.getBulkPrintDocumentFromCaseDocument(documentHelper
                .convertToCaseDocument(caseDetails.getData().get(HEARING_ADDITIONAL_DOC)));
            document.add(additionalUploadedDoc);
        }

        AdditionalHearingDocumentData additionalHearingDocument = additionalHearingDocumentData.get(additionalHearingDocumentData.size() - 1);

        BulkPrintDocument additionalDoc
            = documentHelper.getBulkPrintDocumentFromCaseDocument(additionalHearingDocument.getAdditionalHearingDocument().getDocument());

        document.add(additionalDoc);

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, document);
        }
        if (!notificationService.isRespondentSolicitorRegisteredAndEmailCommunicationEnabled(caseDetails)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, document);
        }
    }

    public CaseDocument convertToPdf(CaseDocument document, String authorisationToken, String caseId) {
        return genericDocumentService.convertDocumentIfNotPdfAlready(document, authorisationToken, caseId);
    }

    private CaseDocument getStampedDocs(String authorisationToken, FinremCaseData caseData, String caseId, CaseDocument uploadDraftDocument) {
        CaseDocument caseDocument = genericDocumentService.convertDocumentIfNotPdfAlready(uploadDraftDocument, authorisationToken, caseId);
        StampType stampType = documentHelper.getStampType(caseData);
        return genericDocumentService.stampDocument(caseDocument, authorisationToken, stampType, caseId);
    }

    public void addToFinalOrderCollection(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData caseData = caseDetails.getData();
        List<DirectionOrderCollection> finalOrderCollection
            = orderDateService.syncCreatedDateAndMarkDocumentStamped(caseData.getFinalOrderCollection(), authorisationToken);

        List<DirectionOrderCollection> uploadHearingOrders = caseData.getUploadHearingOrder();
        if (!uploadHearingOrders.isEmpty()) {
            uploadHearingOrders.forEach(order -> {
                CaseDocument approveOrder = order.getValue().getUploadDraftDocument();
                if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, approveOrder)) {
                    finalOrderCollection.add(order);
                }
            });
        }
        caseData.setFinalOrderCollection(finalOrderCollection);
    }
}
