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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderAdditionalDocCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingOrderDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
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

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ORDER_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;
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
    private final OrderDateService dateService;
    private static final String ADDITIONAL_MESSAGE = "Additional hearing document not required for case: {}";

    public void createAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails) throws JsonProcessingException {
        Map<String, Object> caseData = caseDetails.getData();
        Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
        Map<String, Object> courtDetails = (Map<String, Object>)
            courtDetailsMap.get(caseData.get(CaseHearingFunctions.getSelectedCourt(caseData)));

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);

        prepareHearingCaseDetails(caseDetailsCopy, courtDetails, caseData.get(HEARING_TYPE), caseData.get(HEARING_DATE),
            caseData.get(HEARING_TIME), caseData.get(TIME_ESTIMATE));
        caseDetailsCopy.getData().put("AnyOtherDirections", caseData.get(HEARING_ADDITIONAL_INFO));

        CaseDocument document = generateAdditionalHearingDocument(caseDetailsCopy, authorisationToken);
        addAdditionalHearingDocumentToCaseData(caseDetails, document);
    }

    public void sendAdditionalHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        finremAdditionalHearingCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }

    public void createAndStoreAdditionalHearingDocumentsFromApprovedOrder(String authorisationToken, CaseDetails caseDetails) {
        List<HearingOrderCollectionData> hearingOrderCollectionData = getApprovedHearingOrderCollection(caseDetails);
        String caseId = String.valueOf(caseDetails.getId());
        hearingOrderCollectionData.forEach(hearingOrder ->
            convertHearingOrderCollectionDocumentsToPdf(hearingOrder, authorisationToken, caseId));

        List<HearingOrderCollectionData> hearingOrderStampedCollection = new ArrayList<>();
        Map<String, Object> caseData = caseDetails.getData();
        hearingOrderCollectionData.forEach(hearingOrder -> {
            StampType stampType = documentHelper.getStampType(caseData);
            CaseDocument stampedDocs = genericDocumentService.stampDocument(hearingOrder.getHearingOrderDocuments().getUploadDraftDocument(),
                authorisationToken, stampType, caseId);
            hearingOrderStampedCollection.add(buildHearingOrderDataObject(stampedDocs));
        });

        if (hearingOrderCollectionHasEntries(hearingOrderStampedCollection)) {
            populateLatestDraftHearingOrderWithLatestEntry(caseDetails, hearingOrderStampedCollection);
        }
    }

    private static HearingOrderCollectionData buildHearingOrderDataObject(CaseDocument stampedDocs) {
        return HearingOrderCollectionData.builder()
            .hearingOrderDocuments(HearingOrderDocument.builder().uploadDraftDocument(stampedDocs).build()).build();
    }

    public List<HearingOrderCollectionData> getApprovedHearingOrderCollection(CaseDetails caseDetail) {
        return documentHelper.getHearingOrderDocuments(caseDetail.getData());
    }

    public List<HearingOrderAdditionalDocCollectionData> getHearingOrderAdditionalDocuments(Map<String, Object> caseData) {
        return new ObjectMapper().convertValue(caseData.get(HEARING_UPLOADED_DOCUMENT),
            new TypeReference<>() {
            });
    }

    private boolean hearingOrderCollectionHasEntries(List<HearingOrderCollectionData> hearingOrderCollectionData) {
        return hearingOrderCollectionData != null
            && !hearingOrderCollectionData.isEmpty()
            && hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1).getHearingOrderDocuments() != null;
    }

    private void populateLatestDraftHearingOrderWithLatestEntry(CaseDetails caseDetails,
                                                                List<HearingOrderCollectionData> hearingOrderCollectionData) {
        caseDetails.getData().put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);
        caseDetails.getData().put(LATEST_DRAFT_HEARING_ORDER,
            hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1)
                .getHearingOrderDocuments().getUploadDraftDocument());
    }

    public void sortDirectionDetailsCollection(FinremCaseData caseData) {
        List<DirectionDetailCollection> directionDetailsCollection = Optional.ofNullable(caseData.getDirectionDetailsCollection()).orElse(new ArrayList<>());

        if (!directionDetailsCollection.isEmpty()) {
            List<DirectionDetailCollection> sortedList = directionDetailsCollection
                .stream()
                .filter(e -> (e.getValue().getDateOfHearing() != null))
                .sorted(Comparator.comparing(e -> e.getValue().getDateOfHearing()))
                .toList();
            caseData.setDirectionDetailsCollection(sortedList);
        }
    }

    private DirectionOrderCollection getDirectionOrderCollection(CaseDocument caseDocument, LocalDateTime orderDateTime) {
        return DirectionOrderCollection.builder().value(DirectionOrder.builder()
            .uploadDraftDocument(caseDocument)
            .orderDateTime(orderDateTime)
            .isOrderStamped(YesOrNo.YES)
            .build()).build();
    }

    public void createAndStoreAdditionalHearingDocuments(FinremCaseDetails caseDetails, String authorisationToken)
        throws CourtDetailsParseException, JsonProcessingException {
        log.info("Dealing with caseId {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getData();

        List<DirectionOrderCollection> finalOrderCollection = dateService.addCreatedDateInFinalOrder(caseData.getFinalOrderCollection(), authorisationToken);
        List<DirectionOrderCollection> uploadHearingOrder = dateService.addCreatedDateInUploadedOrder(caseData.getUploadHearingOrder(), authorisationToken);
        if (!uploadHearingOrder.isEmpty()) {
            String caseId = caseDetails.getId().toString();
            List<DirectionOrderCollection> orderCollections = uploadHearingOrder.stream().map(doc -> {
                CaseDocument uploadDraftDocument = doc.getValue().getUploadDraftDocument();
                LocalDateTime orderDateTime = doc.getValue().getOrderDateTime();
                if (!documentHelper.checkIfOrderAlreadyInFinalOrderCollection(finalOrderCollection, uploadDraftDocument)) {
                    CaseDocument stampedDocs = getStampedDocs(authorisationToken, caseData, caseId, uploadDraftDocument);
                    log.info("Stamped Documents = {} for caseId {}", stampedDocs, caseId);
                    if (!finalOrderCollection.isEmpty()) {
                        caseData.getFinalOrderCollection().add(documentHelper.prepareFinalOrder(stampedDocs));
                    } else {
                        caseData.setFinalOrderCollection(List.of(documentHelper.prepareFinalOrder(stampedDocs)));
                    }
                    return getDirectionOrderCollection(stampedDocs, orderDateTime);
                }
                caseData.setFinalOrderCollection(finalOrderCollection);
                //This scenario should not come - when uploaded same order again then stamp order instead leaving unstamped.
                return getDirectionOrderCollection(getStampedDocs(authorisationToken, caseData, caseId, uploadDraftDocument), orderDateTime);
            }).toList();
            caseData.setUploadHearingOrder(orderCollections);
            caseData.setLatestDraftHearingOrder(orderCollections.get(orderCollections.size() - 1).getValue().getUploadDraftDocument());
        }

        List<DirectionDetailCollection> directionDetailsCollection = Optional.ofNullable(caseData.getDirectionDetailsCollection()).orElse(new ArrayList<>());

        //check that the list contains one or more values for the court hearing information
        if (!directionDetailsCollection.isEmpty()) {
            DirectionDetail directionDetail = directionDetailsCollection.get(directionDetailsCollection.size() - 1).getValue();

            //if the latest court hearing has specified another hearing as No, dont create an additional hearing document
            if (NO_VALUE.equalsIgnoreCase(nullToEmpty(directionDetail.getIsAnotherHearingYN()))) {
                log.info(ADDITIONAL_MESSAGE, caseDetails.getId());
                return;
            }

            Map<String, Object> localCourt = directionDetail.getLocalCourt();
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);

            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(
                localCourt.get(CaseHearingFunctions.getSelectedCourtComplexType(localCourt)));

            CaseDetails mapToCaseDetails = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
            CaseDetails caseDetailsCopy = documentHelper.deepCopy(mapToCaseDetails, CaseDetails.class);
            prepareHearingCaseDetails(caseDetailsCopy, courtDetails,
                directionDetail.getTypeOfHearing(),
                directionDetail.getDateOfHearing(),
                directionDetail.getHearingTime(),
                directionDetail.getTimeEstimate());

            CaseDocument document = generateAdditionalHearingDocument(caseDetailsCopy, authorisationToken);
            addAdditionalHearingDocumentToCaseData(mapToCaseDetails, document);
            sortDirectionDetailsCollection(caseData);
        } else {
            log.info(ADDITIONAL_MESSAGE, caseDetails.getId());
        }
    }

    private CaseDocument getStampedDocs(String authorisationToken, FinremCaseData caseData, String caseId, CaseDocument uploadDraftDocument) {
        CaseDocument caseDocument = genericDocumentService.convertDocumentIfNotPdfAlready(uploadDraftDocument, authorisationToken, caseId);
        StampType stampType = documentHelper.getStampType(caseData);
        return genericDocumentService.stampDocument(caseDocument, authorisationToken, stampType, caseId);
    }

    public void createAndStoreAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails)
        throws CourtDetailsParseException, JsonProcessingException {

        List<HearingOrderCollectionData> hearingOrderCollectionData = documentHelper.getHearingOrderDocuments(caseDetails.getData());

        if (hearingOrderCollectionData != null
            && !hearingOrderCollectionData.isEmpty()
            && hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1).getHearingOrderDocuments() != null) {
            String caseId = caseDetails.getId().toString();
            hearingOrderCollectionData.forEach(element ->
                convertHearingOrderCollectionDocumentsToPdf(element, authorisationToken, caseId));
            caseDetails.getData().put(HEARING_ORDER_COLLECTION, hearingOrderCollectionData);
            caseDetails.getData().put(LATEST_DRAFT_HEARING_ORDER,
                hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1)
                    .getHearingOrderDocuments().getUploadDraftDocument());
        }

        List<DirectionDetailsCollectionData> directionDetailsCollectionList = documentHelper
            .convertToDirectionDetailsCollectionData(caseDetails
                .getData()
                .get(DIRECTION_DETAILS_COLLECTION_CT));

        //check that the list contains one or more values for the court hearing information
        if (!directionDetailsCollectionList.isEmpty()) {
            DirectionDetailsCollection latestDirectionDetailsCollectionItem =
                directionDetailsCollectionList.get(directionDetailsCollectionList.size() - 1).getDirectionDetailsCollection();

            //if the latest court hearing has specified another hearing as No, dont create an additional hearing document
            if (NO_VALUE.equalsIgnoreCase(nullToEmpty(latestDirectionDetailsCollectionItem.getIsAnotherHearingYN()))) {
                log.info(ADDITIONAL_MESSAGE, caseDetails.getId());
                return;
            }

            //Otherwise, proceed to extract data from collection
            //Generate and store new additional hearing document using latestDirectionDetailsCollectionItem
            Map<String, Object> courtData = latestDirectionDetailsCollectionItem.getLocalCourt();
            Map<String, Object> courtDetailsMap;

            courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);

            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(
                courtData.get(CaseHearingFunctions.getSelectedCourtComplexType(courtData)));

            CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
            prepareHearingCaseDetails(caseDetailsCopy, courtDetails,
                latestDirectionDetailsCollectionItem.getTypeOfHearing(),
                latestDirectionDetailsCollectionItem.getDateOfHearing(),
                latestDirectionDetailsCollectionItem.getHearingTime(),
                latestDirectionDetailsCollectionItem.getTimeEstimate());

            CaseDocument document = generateAdditionalHearingDocument(caseDetailsCopy, authorisationToken);
            addAdditionalHearingDocumentToCaseData(caseDetails, document);
        } else {
            log.info(ADDITIONAL_MESSAGE, caseDetails.getId());
        }
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

        FrcCourtDetails selectedFRCDetails = FrcCourtDetails.builder()
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

    protected void addAdditionalHearingDocumentToCaseData(CaseDetails caseDetails, CaseDocument document) {
        AdditionalHearingDocumentData generatedDocumentData = AdditionalHearingDocumentData.builder()
            .additionalHearingDocument(AdditionalHearingDocument.builder()
                .document(document)
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

    private void convertHearingOrderCollectionDocumentsToPdf(HearingOrderCollectionData element,
                                                             String authorisationToken, String caseId) {
        CaseDocument pdfApprovedOrder = convertToPdf(element.getHearingOrderDocuments().getUploadDraftDocument(),
            authorisationToken, caseId);
        element.getHearingOrderDocuments().setUploadDraftDocument(pdfApprovedOrder);
    }

    public CaseDocument convertToPdf(CaseDocument document, String authorisationToken, String caseId) {
        return genericDocumentService.convertDocumentIfNotPdfAlready(document, authorisationToken, caseId);
    }
}
