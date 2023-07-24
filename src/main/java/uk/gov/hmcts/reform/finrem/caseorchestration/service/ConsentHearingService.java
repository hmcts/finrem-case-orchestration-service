package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedHearingHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ConsentedHearingDataWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_PROMPT_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_HEARING_COLLECTION_CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourt;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentHearingService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final ConsentedHearingHelper helper;

    public void sendNotification(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        log.info("Hearing notification for case id {}", caseDetails.getId());

        FinremCaseData data = caseDetails.getData();
        if (!data.isPaperCase()) {
            List<ConsentedHearingDataWrapper> listForHearings = data.getListForHearings();
            List<ConsentedHearingDataWrapper> listForHearingsBefore =
                Optional.ofNullable(caseDetailsBefore.getData().getListForHearings()).orElse(new ArrayList<>());
            List<ConsentedHearingDataWrapper> hearingList = listForHearings;


            List<String> hearingIdsToProcess =
                getNewOrDateTimeModifiedHearingIdsList(listForHearings,
                    listForHearingsBefore);

            hearingList.forEach(hearingCaseData -> notify(caseDetails, hearingCaseData, hearingIdsToProcess));
        }
    }

    private void notify(FinremCaseDetails caseDetails, ConsentedHearingDataWrapper hearingCaseData, List<String> hearingIdsToProcess) {
        if (hearingIdsToProcess.contains(hearingCaseData.getId())) {
            Map<String, Object> caseData = helper.convertToMap(hearingCaseData.getValue());
            if (caseDetails.isApplicantSolicitorAgreeToReceiveEmails()) {
                log.info("Sending email notification to Applicant Solicitor about hearing for case id {}", caseDetails.getId());
                notificationService.sendConsentHearingNotificationEmailToApplicantSolicitor(caseDetails, caseData);
                log.info("Email notification to Applicant Solicitor about hearing for case id {} sent.", caseDetails.getId());
            }
            if (caseDetails.getData().isRespondentSolicitorEmailCommunicationEnabled()) {
                log.info("Sending email notification to Respondent Solicitor about hearing for case id {}", caseDetails.getId());
                notificationService.sendConsentHearingNotificationEmailToRespondentSolicitor(caseDetails, caseData);
                log.info("Email notification to Respondent Solicitor about hearing for case id {} sent", caseDetails.getId());
            }
        }
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void submitHearing(CaseDetails caseDetails, CaseDetails caseDetailsBefore, String authorisationToken) {
        log.info("In submit Hearing for case id {}", caseDetails.getId());

        Map<String, Object> caseData = caseDetails.getData();
        List<ConsentedHearingDataWrapper> hearingList = helper.getHearings(caseData);

        log.info("hearingList ::{} for case id {}", hearingList.size(), caseDetails.getId());
        List<String> hearingIdsToProcess =  getNewOrDateTimeModifiedHearingIdsList(caseDetails, caseDetailsBefore);
        log.info("Hearing to Process ::{} for case id {}", hearingIdsToProcess.size(), caseDetails.getId());

        List<BulkPrintDocument> documents = new ArrayList<>();
        List<ConsentedHearingDataWrapper> updatedHearingList =
            hearingList.stream().map(hearingData -> generateHearingDocument(caseDetails,
                hearingData, hearingIdsToProcess, documents, authorisationToken)).toList();

        log.info("Bulk Print list  ::{} for case id {}", documents.size(), caseDetails.getId());
        caseData.put(LIST_FOR_HEARING_COLLECTION_CONSENTED, sortEarliestHearingFirst(updatedHearingList));

        log.info("Sending hearing documents to bulk print for caseid {}", caseDetails.getId());
        sendToBulkPrint(caseDetails, caseData, authorisationToken, documents);
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private void sendToBulkPrint(CaseDetails caseDetails, Map<String, Object> caseData, String authorisationToken,
                                 List<BulkPrintDocument> documents) {

        if (helper.isPaperApplication(caseData) || !helper.isApplicantSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending hearing documents to applicant - bulk print for caseid {}", caseDetails.getId());
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
            log.info("Sent hearing documents to applicant - bulk print for caseid {}", caseDetails.getId());
        }
        if (helper.isPaperApplication(caseData) || !helper.isRespondentSolicitorAgreeToReceiveEmails(caseData)) {
            log.info("Sending hearing documents to respondent - bulk print for caseid {}", caseDetails.getId());
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
            log.info("Sent hearing documents to respondent - bulk print for caseid {}", caseDetails.getId());
        }
    }

    private ConsentedHearingDataWrapper generateHearingDocument(CaseDetails caseDetails,
                                                                ConsentedHearingDataWrapper hearingData,
                                                                List<String> hearingIdsToProcess,
                                                                List<BulkPrintDocument> documents,
                                                                String authorisationToken) {
        if (hearingIdsToProcess.contains(hearingData.getId())) {
            return generateHearingDocument(hearingData, caseDetails, documents, authorisationToken);
        }
        return hearingData;
    }


    @SuppressWarnings("squid:CallToDeprecatedMethod")
    private ConsentedHearingDataWrapper generateHearingDocument(ConsentedHearingDataWrapper hearingData,
                                         CaseDetails caseDetails,
                                         List<BulkPrintDocument> documents,
                                         String authorisationToken) {

        Map<String, Object> hearingCaseData = helper.convertToMap(hearingData.getValue());

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put("ccdCaseNumber", caseDetailsCopy.getId());
        caseData.put("courtDetails", buildFrcCourtDetails(hearingCaseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName",  caseDataService.buildFullRespondentName(caseDetails));
        addHearingVenueDetails(caseDetailsCopy, hearingCaseData);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));
        caseData.put("hearingType", hearingCaseData.get("hearingType"));
        caseData.put("hearingDate", hearingCaseData.get("hearingDate"));
        caseData.put("hearingTime", hearingCaseData.get("hearingTime"));
        caseData.put("hearingTimeEstimate", hearingCaseData.get("hearingTimeEstimate"));
        caseData.put("additionalInformationAboutHearing", hearingCaseData.get("additionalInformationAboutHearing"));

        CaseDocument hearingNotice = genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getHearingNoticeConsentedTemplate(caseDetailsCopy),
            documentConfiguration.getHearingNoticeConsentedFileName());

        hearingData.getValue().setHearingNotice(hearingNotice);
        documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(hearingNotice));
        addToBulkPrintList(caseDetails, hearingCaseData, documents, authorisationToken);

        log.info("HEARING DATA AFTER ADDING HEARING NOTICE ::{}", hearingData);
        return hearingData;
    }

    private void addToBulkPrintList(CaseDetails caseDetails, Map<String, Object> hearingData,
                                    List<BulkPrintDocument> documents,String authorisationToken) {
        String isDocUploaded = nullToEmpty(hearingData.get(HEARING_PROMPT_FOR_DOCUMENT));
        String caseId = caseDetails.getId().toString();
        log.warn("Additional uploaded hearing document found for printing for case id {}", caseId);
        if (YES_VALUE.equalsIgnoreCase(isDocUploaded)) {
            log.warn("Additional uploaded hearing document found for printing for case id {}", caseId);
            CaseDocument caseDocument = documentHelper.convertToCaseDocument(hearingData.get(HEARING_UPLOADED_DOCUMENT));
            CaseDocument additionalUploadedDocuments =
                genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken, caseId);
            documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(additionalUploadedDocuments));
        }
    }

    private void addHearingVenueDetails(CaseDetails caseDetailsCopy, Map<String, Object> hearingCaseData) {
        Map<String, Object> caseData = caseDetailsCopy.getData();
        try {
            log.info("Hearing Case Data {} for caseId {}", hearingCaseData, caseDetailsCopy.getId());
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), new TypeReference<>() {});
            String selectedCourt = getSelectedCourt(hearingCaseData);
            log.info("SELECTED COURT ---> {} for caseId {}", selectedCourt, caseDetailsCopy.getId());//FR_londonList
            String courtDetailsObj = Objects.toString(hearingCaseData.get(selectedCourt), null);
            log.info("HEARING COURT ---> {} for caseId {}", courtDetailsObj, caseDetailsCopy.getId());
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(courtDetailsObj);
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Do not expect any return.
     * <p>Please use @{@link #getNewOrDateTimeModifiedHearingIdsList(List, List)}</p>
     * @param caseDetails instance of CaseDetails
     * @param caseDetailsBefore instance of CaseDetails
     * @deprecated Use {@link Map caseDetails, Map caseDataBefore}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    private List<String> getNewOrDateTimeModifiedHearingIdsList(CaseDetails caseDetails, CaseDetails caseDetailsBefore) {
        List<String> idsList = new ArrayList<>();

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        CaseDetails caseDetailsBeforeCopy = documentHelper.deepCopy(caseDetailsBefore, CaseDetails.class);

        Map<String, Object> caseData = caseDetailsCopy.getData();
        Map<String, Object> caseDataBefore = caseDetailsBeforeCopy.getData();

        List<ConsentedHearingDataWrapper> hearingList = helper.getHearings(caseData);
        log.info("Current Hearing List :: {}", hearingList.size());
        List<ConsentedHearingDataWrapper> beforeHearingList = helper.getHearings(caseDataBefore);
        log.info("beforeHearingList :: {}", beforeHearingList.size());

        hearingList.forEach(data -> idsList.add(data.getId()));
        beforeHearingList.forEach(data -> idsList.remove(data.getId()));

        log.info("after removing List :: {}", idsList.size());

        List<String> modifiedHearingIds = getModifiedHearingIds(caseData, caseDataBefore);

        if (!modifiedHearingIds.isEmpty()) {
            idsList.addAll(modifiedHearingIds);
        }
        log.info("List of new and modified hearings {}", idsList);
        return idsList;
    }

    private List<String> getNewOrDateTimeModifiedHearingIdsList(List<ConsentedHearingDataWrapper> hearingList,
                                                                List<ConsentedHearingDataWrapper> beforeHearingList) {
        List<String> idsList = new ArrayList<>();

        hearingList.forEach(data -> idsList.add(data.getId()));
        beforeHearingList.forEach(data -> idsList.remove(data.getId()));

        log.info("after removing List :: {}", idsList.size());

        List<String> modifiedHearingIds = getModifiedHearingIds(hearingList, beforeHearingList);

        if (!modifiedHearingIds.isEmpty()) {
            idsList.addAll(modifiedHearingIds);
        }
        log.info("List of new and modified hearings {}", idsList);
        return idsList;
    }

    /**
     * Do not expect any return.
     * <p>Please use @{@link #getModifiedHearingIds(List, List)}</p>
     * @param caseData instance of Map
     * @param caseDataBefore instance of Map
     * @deprecated Use {@link Map caseDetails, Map caseDataBefore}
     */
    @Deprecated(since = "15-june-2023")
    @SuppressWarnings("java:S1133")
    private List<String> getModifiedHearingIds(Map<String, Object> caseData, Map<String, Object> caseDataBefore) {
        Map<String, String> currentMap = new HashMap<>();
        Map<String, String> beforeMap = new HashMap<>();
        List<String> modifiedCollectionList = new ArrayList<>();

        List<ConsentedHearingDataWrapper> currentInterimHearingList = helper.getHearings(caseData);
        List<ConsentedHearingDataWrapper> beforeInterimHearingList = helper.getHearings(caseDataBefore);

        if (!currentInterimHearingList.isEmpty() && !beforeInterimHearingList.isEmpty()) {
            currentInterimHearingList.forEach(data -> currentMap.put(data.getId(), String.join("#",
                data.getValue().getHearingDate(), data.getValue().getHearingTime())));

            beforeInterimHearingList.forEach(data -> beforeMap.put(data.getId(), String.join("#",
                data.getValue().getHearingDate(), data.getValue().getHearingTime())));

            log.info("hearing beforeMap::" + beforeMap.size());
            currentMap.entrySet().forEach(currentData -> beforeMap.entrySet()
                .forEach(beforeData -> setList(currentData, beforeData, modifiedCollectionList)));
        }

        log.info("Modified hearing collection list::" + modifiedCollectionList);
        return modifiedCollectionList;
    }

    private List<String> getModifiedHearingIds(List<ConsentedHearingDataWrapper> currentInterimHearingList,
                                               List<ConsentedHearingDataWrapper> beforeInterimHearingList) {
        Map<String, String> currentMap = new HashMap<>();
        Map<String, String> beforeMap = new HashMap<>();
        List<String> modifiedCollectionList = new ArrayList<>();

        if (!currentInterimHearingList.isEmpty() && !beforeInterimHearingList.isEmpty()) {
            currentInterimHearingList.forEach(data -> currentMap.put(data.getId(), String.join("#",
                data.getValue().getHearingDate(), data.getValue().getHearingTime())));

            beforeInterimHearingList.forEach(data -> beforeMap.put(data.getId(), String.join("#",
                data.getValue().getHearingDate(), data.getValue().getHearingTime())));

            log.info("hearing beforeMap::" + beforeMap.size());
            currentMap.entrySet().forEach(currentData -> beforeMap.entrySet()
                .forEach(beforeData -> setList(currentData, beforeData, modifiedCollectionList)));
        }

        log.info("Modified hearing collection list::" + modifiedCollectionList);
        return modifiedCollectionList;
    }

    private  void setList(Map.Entry<String, String> currentDataMap, Map.Entry<String, String> beforeDataMap,
                          List<String> modifiedCollectionList) {
        if (currentDataMap.getKey().equals(beforeDataMap.getKey()) && ! currentDataMap.getValue().equals(beforeDataMap.getValue())) {
            modifiedCollectionList.add(currentDataMap.getKey());
        }
    }

    private List<ConsentedHearingDataWrapper> sortEarliestHearingFirst(List<ConsentedHearingDataWrapper> hearingList) {
        return hearingList.stream()
            .sorted(Comparator.nullsLast(Comparator.comparing(e -> e.getValue().getHearingDate())))
            .toList();
    }
}
