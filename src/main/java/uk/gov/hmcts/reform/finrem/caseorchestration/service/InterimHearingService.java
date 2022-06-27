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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingCollectionItemData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItems;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TRACKING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_WALES_FRC_COURT_LIST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
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
    private final CaseDataService caseDataService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    public void submitInterimHearing(CaseDetails caseDetails, String authorisationToken) {
        log.info("In submitInterimHearing for case id {}", caseDetails.getId());
        Map<String, Object> caseData = caseDetails.getData();
        List<InterimHearingData> interimHearingList = filterInterimHearingToProcess(caseData);

        List<BulkPrintDocument> documents = prepareDocumentsForPrint(caseDetails, interimHearingList, authorisationToken);
        sendToBulkPrint(caseDetails, caseData, authorisationToken, documents);

        //Need only for existing Interim Hearing
        if (caseData.get(INTERIM_HEARING_TYPE) != null) {
            removeNonCollectionInterimData(caseData);
        }
        caseDetails.setData(caseData);
    }

    private void sendToBulkPrint(CaseDetails caseDetails, Map<String, Object> caseData, String authorisationToken,
                                 List<BulkPrintDocument> documents) {
        if (isPaperApplication(caseData) || !isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        }
        if (isPaperApplication(caseData) || !isRespondentSolicitorAgreeToReceiveEmails(caseData)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
        }
    }

    private List<BulkPrintDocument> prepareDocumentsForPrint(CaseDetails caseDetails,
                                                             List<InterimHearingData> interimHearingList,
                                                             String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        List<CaseDocument> interimDocument = prepareInterimHearingRequiredNoticeDocument(caseDetails,
            interimHearingList, authorisationToken);
        List<BulkPrintDocument> documents = interimDocument.stream()
            .map(documentHelper::getCaseDocumentAsBulkPrintDocument).collect(Collectors.toList());

        addUploadedDocumentsToBulkPrintList(caseData, interimHearingList, documents, authorisationToken);

        return documents;
    }

    private void addUploadedDocumentsToBulkPrintList(Map<String, Object> caseData,
                                                     List<InterimHearingData> interimHearingList,
                                                     List<BulkPrintDocument> documents,
                                                     String authorisationToken) {
        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(interimHearingList);
        interimCaseData.forEach(interimData -> addToBulkPrintList(caseData, interimData, documents, authorisationToken));
    }

    private void addToBulkPrintList(Map<String, Object> caseData, Map<String, Object> interimData,
                                      List<BulkPrintDocument> documents,String authorisationToken) {
        String isDocUploaded = nullToEmpty(interimData.get(INTERIM_HEARING_PROMPT_FOR_DOCUMENT));
        if ("Yes".equalsIgnoreCase(isDocUploaded)) {
            log.warn("Additional uploaded interim document found for printing for case");
            CaseDocument caseDocument = documentHelper.convertToCaseDocument(interimData.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
            CaseDocument additionalUploadedDocuments = genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken);
            documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(additionalUploadedDocuments));

            List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList = Optional.ofNullable(caseData.get(INTERIM_HEARING_ALL_DOCUMENT))
                .map(this::convertToBulkPrintDocumentDataList).orElse(new ArrayList<>());

            bulkPrintDocumentsList.add(loadBulkPrintDocument(additionalUploadedDocuments));
            caseData.put(INTERIM_HEARING_ALL_DOCUMENT, bulkPrintDocumentsList);
        }
    }

    private InterimHearingBulkPrintDocumentsData loadBulkPrintDocument(CaseDocument additionalUploadedDocuments) {

        return InterimHearingBulkPrintDocumentsData.builder().id(UUID.randomUUID().toString())
            .value(InterimHearingBulkPrintDocument.builder()
                .caseDocument(CaseDocument.builder()
                    .documentUrl(additionalUploadedDocuments.getDocumentUrl())
                    .documentFilename(additionalUploadedDocuments.getDocumentFilename())
                    .documentBinaryUrl(additionalUploadedDocuments.getDocumentBinaryUrl())
                .build()).build())
            .build();
    }

    private List<InterimHearingBulkPrintDocumentsData> convertToBulkPrintDocumentDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private List<CaseDocument> prepareInterimHearingRequiredNoticeDocument(CaseDetails caseDetails,
                                                                     List<InterimHearingData> interimHearingList,
                                                                     String authorisationToken) {

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(interimHearingList);

        return interimCaseData.stream()
            .map(data -> generateCaseDocument(caseData, data, caseDetailsCopy, authorisationToken))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> convertInterimHearingCollectionDataToMap(List<InterimHearingData> interimHearingList) {
        List<InterimHearingItems> interimHearingItems
            = interimHearingList.stream().map(InterimHearingData::getValue).collect(Collectors.toList());
        return interimHearingItems.stream()
            .map(obj -> objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
            })).collect(Collectors.toList());
    }

    private CaseDocument generateCaseDocument(Map<String, Object> caseData, Map<String, Object> interimHearingCaseData,
                                    CaseDetails caseDetailsCopy,
                                    String authorisationToken) {

        caseData.put("ccdCaseNumber", caseDetailsCopy.getId());
        caseData.put("courtDetails", buildInterimHearingFrcCourtDetails(interimHearingCaseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        addInterimHearingVenueDetails(caseDetailsCopy, interimHearingCaseData);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));
        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());

    }

    private void addInterimHearingVenueDetails(CaseDetails caseDetails, Map<String, Object> interimHearingCaseData) {
        Map<String, Object> caseData = caseDetails.getData();
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), new TypeReference<>() {});
            String selectedCourtIH = getSelectedCourtIH(interimHearingCaseData);
            String courtDetailsObj = (String) interimHearingCaseData.get(selectedCourtIH);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(courtDetailsObj);
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private boolean isPaperApplication(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(PAPER_APPLICATION)));
    }

    private boolean isRespondentRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONTESTED_RESPONDENT_REPRESENTED)));
    }

    private boolean isApplicantSolicitorAgreeToReceiveEmails(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED)));
    }

    private boolean isRespondentSolicitorAgreeToReceiveEmails(Map<String, Object> caseData) {
        return !isPaperApplication(caseData)
            && isRespondentRepresentedByASolicitor(caseData)
            && isNotEmpty(RESP_SOLICITOR_EMAIL, caseData)
            && !NO_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT)));
    }

    private List<InterimHearingData> convertToInterimHearingDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    public List<InterimHearingData> filterInterimHearingToProcess(Map<String, Object> caseData) {
        List<InterimHearingData> sortedInterimHearingList = sortEarliestHearingFirst(caseData);
        caseData.put(INTERIM_HEARING_COLLECTION, sortedInterimHearingList);

        List<InterimHearingCollectionItemData> trackingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_TRACKING))
            .map(this::convertToInterimHearingCollectionItemDataList).orElse(new ArrayList<>());

        List<String> alreadyProcessedIds = trackingList.stream()
            .map(existingCollectionId -> existingCollectionId.getValue().getIhItemIds()).collect(Collectors.toList());

        return sortedInterimHearingList.stream()
            .filter(collectionId -> !alreadyProcessedIds.contains(collectionId.getId()))
            .collect(Collectors.toList());
    }

    private List<InterimHearingData> sortEarliestHearingFirst(Map<String, Object> caseData) {
        List<InterimHearingData> interimHearingList = Optional.ofNullable(caseData.get(INTERIM_HEARING_COLLECTION))
            .map(this::convertToInterimHearingDataList).orElse(Collections.emptyList());

        return interimHearingList.stream()
            .sorted(Comparator.nullsLast(Comparator.comparing(e -> e.getValue().getInterimHearingDate())))
            .collect(Collectors.toList());
    }

    private List<InterimHearingCollectionItemData> convertToInterimHearingCollectionItemDataList(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
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

    public void sendNotification(CaseDetails caseDetails) {
        log.info("Sending email notification for case id {}", caseDetails.getId());

        Map<String, Object> caseData =  caseDetails.getData();
        if (!caseDataService.isPaperApplication(caseData)) {
            List<InterimHearingData> caseDataList = filterInterimHearingToProcess(caseData);
            List<Map<String, Object>> interimCaseData = convertInterimHearingCollectionDataToMap(caseDataList);
            interimCaseData.forEach(interimHearingData -> notify(caseDetails, interimHearingData));
        }
    }

    private void notify(CaseDetails caseDetails, Map<String, Object> interimHearingData) {
        if (caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            log.info("Sending email notification to Applicant Solicitor about interim hearing");
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.shouldEmailRespondentSolicitor(caseDetails.getData())) {
            log.info("Sending email notification to Respondent Solicitor about interim hearing");
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(caseDetails, interimHearingData);
        }
    }
}
