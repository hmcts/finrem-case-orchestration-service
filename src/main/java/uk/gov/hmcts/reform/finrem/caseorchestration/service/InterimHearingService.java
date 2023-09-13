package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.InterimHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNotice;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.IntervenerHearingNoticeCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SelectablePartiesCorrespondenceService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_ALL_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BIRMINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_BRISTOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CFC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_CLEAVELAND_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DEVON_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DORSET_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_HUMBER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_KENT_SURREY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LANCASHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LIVERPOOL_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_LONDON_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MANCHESTER_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NEWPORT_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWALES_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NORTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NOTTINGHAM_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_NWYORKSHIRE_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_REGION_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHEAST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SOUTHWEST_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_SWANSEA_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_THAMESVALLEY_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildInterimHearingFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtIH;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterimHearingService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final InterimHearingHelper interimHearingHelper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    private final SelectablePartiesCorrespondenceService selectablePartiesCorrespondenceService;

    public void submitInterimHearing(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authorisationToken) {
        log.info("In submitInterimHearing for case id {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();
        Map<String, Object> caseDataBefore = caseDetailsBefore.getData();
        List<InterimHearingData> interimHearingList = filterInterimHearingToProcess(caseData, caseDataBefore);

        if (!interimHearingList.isEmpty()) {
            CaseDocumentsHolder caseDocumentsHolder = prepareDocumentsForPrint(caseDetails, interimHearingList, authorisationToken);
            sendToBulkPrint(caseDetails, authorisationToken, caseDocumentsHolder);
        }

        //Need only for existing Interim Hearing
        if (caseData.get(INTERIM_HEARING_TYPE) != null) {
            removeNonCollectionInterimData(caseData);
        }
        caseDetails.setData(caseData);
    }


    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private void sendToBulkPrint(CaseDetails caseDetails, String authorisationToken,
                                 CaseDocumentsHolder caseDocumentsHolder) {

        if ((!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails))
            && selectablePartiesCorrespondenceService.shouldSendApplicantCorrespondence(caseDetails)) {
            log.info("Sending interim hearing documents to applicant - bulk print for caseid {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, caseDocumentsHolder.getBulkPrintDocuments());
        }
        if ((!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails))
            && selectablePartiesCorrespondenceService.shouldSendRespondentCorrespondence(caseDetails)) {
            log.info("Sending interim hearing documents to respondent - bulk print for caseid {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, caseDocumentsHolder.getBulkPrintDocuments());
        }
        sendToBulkPrintForInterveners(authorisationToken, caseDetails, caseDocumentsHolder);
    }

    private void sendToBulkPrintForInterveners(String authorisationToken, CaseDetails caseDetails, CaseDocumentsHolder caseDocumentsHolder) {
        final FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        selectablePartiesCorrespondenceService.setPartiesToReceiveCorrespondence(finremCaseDetails.getData());
        final List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
        interveners.forEach(intervenerWrapper -> {
            if (intervenerWrapper.getIntervenerCorrespondenceEnabled() != null
                && Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled())) {
                addCaseDocumentsToIntervenerHearingNotices(intervenerWrapper, caseDocumentsHolder, finremCaseDetails.getData(),
                    caseDetails.getData());
                if (!notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails)) {
                    log.info("Sending letter correspondence to {} for case: {}",
                        intervenerWrapper.getIntervenerType().getTypeValue(),
                        caseDetails.getId());
                    bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken,
                        caseDocumentsHolder.getBulkPrintDocuments());
                }
            }
        });
    }

    private void addCaseDocumentsToIntervenerHearingNotices(IntervenerWrapper intervenerWrapper, CaseDocumentsHolder caseDocumentsHolder,
                                                            FinremCaseData finremCaseData, Map<String, Object> caseData) {
        List<IntervenerHearingNoticeCollection> hearingNotices = intervenerWrapper.getIntervenerHearingNoticesCollection(finremCaseData);
        caseDocumentsHolder.getCaseDocuments().forEach(cd -> {
            hearingNotices.add(getHearingNoticesDocumentCollection(cd));
        });
        caseData.put(intervenerWrapper.getIntervenerHearingNoticesCollectionName(), hearingNotices);
    }

    private IntervenerHearingNoticeCollection getHearingNoticesDocumentCollection(CaseDocument hearingNotice) {
        return IntervenerHearingNoticeCollection.builder()
            .value(IntervenerHearingNotice.builder().caseDocument(hearingNotice)
                .noticeReceivedAt(LocalDateTime.now()).build()).build();
    }

    @SuppressWarnings("java:S6204")
    private CaseDocumentsHolder prepareDocumentsForPrint(CaseDetails caseDetails,
                                                         List<InterimHearingData> interimHearingList,
                                                         String authorisationToken) {
        CaseDocumentsHolder caseDocumentsHolder = CaseDocumentsHolder.builder()
            .caseDocuments(new ArrayList<>())
            .bulkPrintDocuments(new ArrayList<>())
            .build();

        String caseId = caseDetails.getId().toString();
        log.info("preparing for bulk print document for case id {}", caseId);
        Map<String, Object> caseData = caseDetails.getData();
        List<CaseDocument> interimDocument = prepareInterimHearingRequiredNoticeDocument(caseDetails,
            interimHearingList, authorisationToken);
        caseDocumentsHolder.getCaseDocuments().addAll(interimDocument);

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            interimHearingHelper.getInterimHearingBulkPrintDocumentList(caseData);

        interimDocument.forEach(doc -> bulkPrintDocumentsList.add(loadBulkPrintDocument(doc)));


        caseData.put(INTERIM_HEARING_ALL_DOCUMENT, bulkPrintDocumentsList);

        List<BulkPrintDocument> documents = interimDocument.stream()
            .map(documentHelper::getCaseDocumentAsBulkPrintDocument).collect(Collectors.toList());

        addUploadedDocumentsToBulkPrintList(caseId, interimHearingList, caseDocumentsHolder, authorisationToken);

        return caseDocumentsHolder;
    }

    private void addUploadedDocumentsToBulkPrintList(String caseId,
                                                     List<InterimHearingData> interimHearingList,
                                                     CaseDocumentsHolder caseDocumentsHolder,
                                                     String authorisationToken) {
        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(interimHearingList);
        interimCaseData.forEach(interimData -> addToBulkPrintList(caseId, interimData, caseDocumentsHolder, authorisationToken));
    }

    private void addToBulkPrintList(String caseId, Map<String, Object> interimData,
                                    CaseDocumentsHolder caseDocumentsHolder, String authorisationToken) {
        String isDocUploaded = nullToEmpty(interimData.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT));
        if ("Yes".equalsIgnoreCase(isDocUploaded)) {
            log.warn("Additional uploaded interim document found for printing for case id {}", caseId);
            CaseDocument caseDocument =
                documentHelper.convertToCaseDocument(interimData.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
            CaseDocument additionalUploadedDocuments =
                genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken, caseId);
            caseDocumentsHolder.getBulkPrintDocuments().add(documentHelper.getCaseDocumentAsBulkPrintDocument(additionalUploadedDocuments));
            caseDocumentsHolder.getCaseDocuments().add(additionalUploadedDocuments);
        }
    }

    private InterimHearingBulkPrintDocumentsData loadBulkPrintDocument(CaseDocument generatedDocument) {

        return InterimHearingBulkPrintDocumentsData.builder().id(UUID.randomUUID().toString())
            .value(InterimHearingBulkPrintDocument.builder()
                .caseDocument(CaseDocument.builder()
                    .documentUrl(generatedDocument.getDocumentUrl())
                    .documentFilename(generatedDocument.getDocumentFilename())
                    .documentBinaryUrl(generatedDocument.getDocumentBinaryUrl())
                    .build()).build())
            .build();
    }

    private List<CaseDocument> prepareInterimHearingRequiredNoticeDocument(CaseDetails caseDetails,
                                                                           List<InterimHearingData> interimHearingList,
                                                                           String authorisationToken) {

        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(interimHearingList);

        return interimCaseData.stream()
            .map(interimHearingCaseData -> generateCaseDocument(interimHearingCaseData, caseDetails, authorisationToken))
            .toList();
    }

    @SuppressWarnings("java:S6204")
    public List<Map<String, Object>> convertInterimHearingCollectionDataToMap(List<InterimHearingData> interimHearingList) {
        List<InterimHearingItem> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).collect(Collectors.toList());
        return interimHearingItems.stream()
            .map(obj -> objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).toList();
    }

    private CaseDocument generateCaseDocument(Map<String, Object> interimHearingCaseData,
                                              CaseDetails caseDetails,
                                              String authorisationToken) {

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put("ccdCaseNumber", caseDetailsCopy.getId());
        caseData.put("courtDetails", buildInterimHearingFrcCourtDetails(interimHearingCaseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        addInterimHearingVenueDetails(caseDetailsCopy, interimHearingCaseData);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));
        caseData.put("interimHearingType", interimHearingCaseData.get("interimHearingType"));
        caseData.put("interimHearingDate", interimHearingCaseData.get("interimHearingDate"));
        caseData.put("interimHearingTime", interimHearingCaseData.get("interimHearingTime"));
        caseData.put("interimTimeEstimate", interimHearingCaseData.get("interimHearingTimeEstimate"));
        caseData.put("interimAdditionalInformationAboutHearing", interimHearingCaseData.get("interimAdditionalInformationAboutHearing"));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(caseDetailsCopy),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());

    }

    private void addInterimHearingVenueDetails(CaseDetails caseDetailsCopy, Map<String, Object> interimHearingCaseData) {
        Map<String, Object> caseData = caseDetailsCopy.getData();
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), new TypeReference<>() {
            });
            String selectedCourtIH = getSelectedCourtIH(interimHearingCaseData);
            String courtDetailsObj = (String) interimHearingCaseData.get(selectedCourtIH);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(courtDetailsObj);
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public List<InterimHearingData> filterInterimHearingToProcess(Map<String, Object> caseData, Map<String, Object> caseDataBefore) {
        List<InterimHearingData> sortedInterimHearingList = sortEarliestHearingFirst(caseData);
        caseData.put(INTERIM_HEARING_COLLECTION, sortedInterimHearingList);

        List<InterimHearingCollectionItemData> trackingList = interimHearingHelper.getInterimHearingTrackingList(caseData);

        List<String> dataToProcessList = compareCaseData(caseData, caseDataBefore);


        log.info("filterInterimHearingToProcess :: trackingList {}", trackingList.size());
        List<String> alreadyProcessedIds = trackingList.stream()
            .map(existingCollectionId -> existingCollectionId.getValue().getIhItemIds()).collect(Collectors.toList());

        alreadyProcessedIds.removeAll(dataToProcessList);

        return sortedInterimHearingList.stream()
            .filter(collectionId -> !alreadyProcessedIds.contains(collectionId.getId()))
            .toList();
    }

    private List<String> compareCaseData(Map<String, Object> caseData, Map<String, Object> caseDataBefore) {
        Map<String, String> currentMap = new HashMap<>();
        Map<String, String> beforeMap = new HashMap<>();
        List<String> modifiedCollectionList = new ArrayList<>();

        List<InterimHearingData> currentInterimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);
        List<InterimHearingData> beforeInterimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseDataBefore);

        String beforeMigrationHearingDate = nullToEmpty(caseData.get(INTERIM_HEARING_DATE));
        String beforeMigrationHearingTime = nullToEmpty(caseData.get(INTERIM_HEARING_TIME));

        if (!currentInterimHearingList.isEmpty() && (!beforeInterimHearingList.isEmpty()
            || !beforeMigrationHearingDate.isEmpty())) {
            currentInterimHearingList.forEach(data -> currentMap.put(data.getId(), String.join("#",
                data.getValue().getInterimHearingDate(), data.getValue().getInterimHearingTime())));

            log.info("Non collection hearing date {} and time {}", beforeMigrationHearingDate, beforeMigrationHearingTime);
            if (beforeMigrationHearingDate.isEmpty() && beforeMigrationHearingTime.isEmpty()) {
                beforeInterimHearingList.forEach(data -> beforeMap.put(data.getId(), String.join("#",
                    data.getValue().getInterimHearingDate(), data.getValue().getInterimHearingTime())));
            } else {
                currentInterimHearingList.forEach(data -> beforeMap.put(data.getId(), String.join("#",
                    beforeMigrationHearingDate, beforeMigrationHearingTime)));
            }
            log.info("beforeMap::" + beforeMap.size());
            currentMap.entrySet().forEach(currentData -> beforeMap.entrySet()
                .forEach(beforeData -> setList(currentData, beforeData, modifiedCollectionList)));
        }

        log.info("Modified collection list::" + modifiedCollectionList);
        return modifiedCollectionList;
    }

    private void setList(Map.Entry<String, String> currentDataMap, Map.Entry<String, String> beforeDataMap,
                         List<String> modifiedCollectionList) {
        if (currentDataMap.getKey().equals(beforeDataMap.getKey()) && !currentDataMap.getValue().equals(beforeDataMap.getValue())) {
            modifiedCollectionList.add(currentDataMap.getKey());
        }
    }

    private List<InterimHearingData> sortEarliestHearingFirst(Map<String, Object> caseData) {
        List<InterimHearingData> interimHearingList = interimHearingHelper.isThereAnExistingInterimHearing(caseData);
        return interimHearingList.stream()
            .sorted(Comparator.nullsLast(Comparator.comparing(e -> e.getValue().getInterimHearingDate())))
            .toList();
    }

    private void removeNonCollectionInterimData(Map<String, Object> caseData) {
        caseData.remove(INTERIM_HEARING_TYPE);
        caseData.remove(INTERIM_HEARING_DATE);
        caseData.remove(INTERIM_HEARING_TIME);
        caseData.remove(INTERIM_HEARING_TIME_ESTIMATE);
        caseData.remove(INTERIM_HEARING_REGION_LIST);
        caseData.remove(INTERIM_HEARING_CFC_COURT_LIST);
        caseData.remove(INTERIM_HEARING_WALES_FRC_COURT_LIST);
        caseData.remove(INTERIM_HEARING_LONDON_FRC_COURT_LIST);
        caseData.remove(INTERIM_HEARING_DEVON_COURT_LIST);
        caseData.remove(INTERIM_HEARING_DORSET_COURT_LIST);
        caseData.remove(INTERIM_HEARING_HUMBER_COURT_LIST);
        caseData.remove(INTERIM_HEARING_MIDLANDS_FRC_COURT_LIST);
        caseData.remove(INTERIM_HEARING_BRISTOL_COURT_LIST);
        caseData.remove(INTERIM_HEARING_NEWPORT_COURT_LIST);
        caseData.remove(INTERIM_HEARING_NORTHEAST_COURT_LIST);
        caseData.remove(INTERIM_HEARING_NORTHWEST_COURT_LIST);
        caseData.remove(INTERIM_HEARING_SOUTHEAST_COURT_LIST);
        caseData.remove(INTERIM_HEARING_SOUTHWEST_COURT_LIST);
        caseData.remove(INTERIM_HEARING_SWANSEA_COURT_LIST);
        caseData.remove(INTERIM_HEARING_LIVERPOOL_COURT_LIST);
        caseData.remove(INTERIM_HEARING_BIRMINGHAM_COURT_LIST);
        caseData.remove(INTERIM_HEARING_CLEAVELAND_COURT_LIST);
        caseData.remove(INTERIM_HEARING_KENT_SURREY_COURT_LIST);
        caseData.remove(INTERIM_HEARING_LANCASHIRE_COURT_LIST);
        caseData.remove(INTERIM_HEARING_MANCHESTER_COURT_LIST);
        caseData.remove(INTERIM_HEARING_NORTHWALES_COURT_LIST);
        caseData.remove(INTERIM_HEARING_NOTTINGHAM_COURT_LIST);
        caseData.remove(INTERIM_HEARING_NWYORKSHIRE_COURT_LIST);
        caseData.remove(INTERIM_HEARING_BEDFORDSHIRE_COURT_LIST);
        caseData.remove(INTERIM_HEARING_THAMESVALLEY_COURT_LIST);
        caseData.remove(INTERIM_HEARING_ADDITIONAL_INFO);
        caseData.remove(INTERIM_HEARING_PROMPT_FOR_DOCUMENT);
        caseData.remove(INTERIM_HEARING_UPLOADED_DOCUMENT);
        caseData.remove(INTERIM_HEARING_DOCUMENT);
    }

    public void sendNotification(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        log.info("Sending email notification for case id {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();
        Map<String, Object> caseDataBefore = caseDetailsBefore.getData();
        List<InterimHearingData> caseDataList = filterInterimHearingToProcess(caseData, caseDataBefore);
        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(caseDataList);
        interimCaseData.forEach(interimHearingData -> notify(caseDetails, interimHearingData));

    }

    private void notify(CaseDetails caseDetails, Map<String, Object> interimHearingData) {
        if (notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor about interim hearing for case id {}", caseDetails.getId());
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)) {
            log.info("Sending email notification to Respondent Solicitor about interim hearing for case id {}", caseDetails.getId());
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.isContestedApplication(caseDetails)) {
            final FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
            final List<IntervenerWrapper> interveners = finremCaseDetails.getData().getInterveners();
            interveners.forEach(intervenerWrapper -> {
                if (notificationService.isIntervenerSolicitorDigitalAndEmailPopulated(intervenerWrapper, caseDetails)
                    && (intervenerWrapper.getIntervenerCorrespondenceEnabled() != null
                    && Boolean.TRUE.equals(intervenerWrapper.getIntervenerCorrespondenceEnabled()))) {
                    log.info("Sending email notification to {} Solicitor about interim hearing for case id {}",
                        intervenerWrapper.getIntervenerType().getTypeValue(),
                        caseDetails.getId());
                    notificationService.sendInterimHearingNotificationEmailToIntervenerSolicitor(caseDetails, interimHearingData,
                        notificationService.getCaseDataKeysForIntervenerSolicitor(intervenerWrapper));
                }
            });
        }
    }


}
