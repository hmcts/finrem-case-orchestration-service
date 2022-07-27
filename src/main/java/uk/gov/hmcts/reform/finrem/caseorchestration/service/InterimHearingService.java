package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.interimhearing.GeneralApplicationInterimHearingNoticeDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingBulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingBulkPrintDocumentsData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollectionItemData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo.isYes;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterimHearingService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final NotificationService notificationService;
    private final DocumentHelper documentHelper;
    private final GeneralApplicationInterimHearingNoticeDetailsMapper generalApplicationInterimHearingNoticeDetailsMapper;

    public void submitInterimHearing(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore, String authorisationToken) {
        log.info("In submitInterimHearing for case id {}", caseDetails.getId());
        FinremCaseData caseData = caseDetails.getCaseData();
        FinremCaseData caseDataBefore = caseDetailsBefore.getCaseData();
        List<InterimHearingCollection> interimHearingList = filterInterimHearingToProcess(caseData, caseDataBefore);

        if (!interimHearingList.isEmpty()) {
            List<BulkPrintDocument> documents = prepareDocumentsForPrint(caseDetails, interimHearingList, authorisationToken);
            sendToBulkPrint(caseDetails, caseData, authorisationToken, documents);
        }

        //Need only for existing Interim Hearing
        if (caseData.getInterimWrapper().getInterimHearingType() != null) {
            removeNonCollectionInterimData(caseData);
        }
    }

    private void sendToBulkPrint(FinremCaseDetails caseDetails, FinremCaseData caseData, String authorisationToken,
                                 List<BulkPrintDocument> documents) {
        if (caseData.isPaperCase() || !caseData.isApplicantSolicitorAgreeToReceiveEmails()) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        }
        if (caseData.isPaperCase() || !isRespondentSolicitorAgreeToReceiveEmails(caseData)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
        }
    }

    private List<BulkPrintDocument> prepareDocumentsForPrint(FinremCaseDetails caseDetails,
                                                             List<InterimHearingCollection> interimHearingList,
                                                             String authorisationToken) {
        FinremCaseData caseData = caseDetails.getCaseData();
        List<Document> interimDocument = prepareInterimHearingRequiredNoticeDocument(caseDetails,
            interimHearingList, authorisationToken);

        List<InterimHearingBulkPrintDocumentsData> bulkPrintDocumentsList =
            Optional.ofNullable(caseData.getInterimWrapper().getInterimHearingDocuments()).orElse(new ArrayList<>());

        interimDocument.forEach(doc -> bulkPrintDocumentsList.add(loadBulkPrintDocument(doc)));
        caseData.getInterimWrapper().setInterimHearingDocuments(bulkPrintDocumentsList);

        List<BulkPrintDocument> documents = interimDocument.stream()
            .map(documentHelper::getDocumentAsBulkPrintDocument)
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

        addUploadedDocumentsToBulkPrintList(interimHearingList, documents, authorisationToken);

        return documents;
    }

    private void addUploadedDocumentsToBulkPrintList(List<InterimHearingCollection> interimHearingList,
                                                     List<BulkPrintDocument> documents,
                                                     String authorisationToken) {
        interimHearingList.forEach(interimData -> addToBulkPrintList(interimData, documents, authorisationToken));
    }

    private void addToBulkPrintList(InterimHearingCollection interimData,
                                      List<BulkPrintDocument> documents,
                                    String authorisationToken) {
        String isDocUploaded = interimData.getValue().getInterimPromptForAnyDocument();
        if (isYes(isDocUploaded)) {
            log.warn("Additional uploaded interim document found for printing for case");
            Document caseDocument = interimData.getValue().getInterimUploadAdditionalDocument();
            Document additionalUploadedDocuments = genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken);
            documents.add(documentHelper.getDocumentAsBulkPrintDocument(additionalUploadedDocuments).orElse(null));
        }
    }

    private InterimHearingBulkPrintDocumentsData loadBulkPrintDocument(Document generatedDocument) {

        return InterimHearingBulkPrintDocumentsData.builder().id(UUID.randomUUID().toString())
            .value(InterimHearingBulkPrintDocument.builder()
                .bulkPrintDocument(Document.builder()
                    .url(generatedDocument.getUrl())
                    .filename(generatedDocument.getFilename())
                    .binaryUrl(generatedDocument.getBinaryUrl())
                .build()).build())
            .build();
    }

    private List<Document> prepareInterimHearingRequiredNoticeDocument(FinremCaseDetails caseDetails,
                                                                     List<InterimHearingCollection> interimHearingList,
                                                                     String authorisationToken) {
        return interimHearingList.stream()
            .map(data -> generateCaseDocument(caseDetails, data, authorisationToken))
            .collect(Collectors.toList());
    }

    private Document generateCaseDocument(FinremCaseDetails caseDetails, InterimHearingCollection hearingItem,
                                    String authorisationToken) {
        Map<String, Object> placeholdersMap = generalApplicationInterimHearingNoticeDetailsMapper
            .getDocumentTemplateDetailsFromCollectionItem(caseDetails, hearingItem);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, placeholdersMap,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());

    }

    private boolean isRespondentSolicitorAgreeToReceiveEmails(FinremCaseData caseData) {
        return !caseData.isPaperCase()
            && caseData.isRespondentRepresentedByASolicitor()
            && isNotEmpty(caseData.getContactDetailsWrapper().getRespondentSolicitorEmail())
            && caseData.isRespondentSolicitorAgreeToReceiveEmails();
    }

    public boolean isNotEmpty(String field) {
        return StringUtils.isNotEmpty(field);
    }

    public List<InterimHearingCollection> filterInterimHearingToProcess(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        List<InterimHearingCollection> sortedInterimHearingList = sortEarliestHearingFirst(caseData);
        caseData.getInterimWrapper().setInterimHearings(sortedInterimHearingList);

        List<InterimHearingCollectionItemData> trackingList = caseData.getInterimWrapper().getInterimHearingCollectionItemIds();

        List<String> dataToProcessList = compareCaseData(caseData, caseDataBefore);


        log.info("filterInterimHearingToProcess :: trackingList {}", trackingList.size());
        List<String> alreadyProcessedIds = trackingList.stream()
            .map(existingCollectionId -> existingCollectionId.getValue().getIhItemIds()).collect(Collectors.toList());

        alreadyProcessedIds.removeAll(dataToProcessList);

        return sortedInterimHearingList.stream()
            .filter(collectionId -> !alreadyProcessedIds.contains(collectionId.getId().toString()))
            .collect(Collectors.toList());
    }

    private List<String> compareCaseData(FinremCaseData caseData, FinremCaseData caseDataBefore) {
        Map<String, String> currentMap = new HashMap<>();
        Map<String, String> beforeMap = new HashMap<>();
        List<String> modifiedCollectionList = new ArrayList<>();

        List<InterimHearingCollection> currentInterimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());
        List<InterimHearingCollection> beforeInterimHearingList = Optional.ofNullable(caseDataBefore.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());

        String beforeMigrationHearingDate = nullToEmpty(caseData.getInterimWrapper().getInterimHearingDate());
        String beforeMigrationHearingTime = nullToEmpty(caseData.getInterimWrapper().getInterimHearingTime());

        if (!currentInterimHearingList.isEmpty() && (!beforeInterimHearingList.isEmpty()
            || !beforeMigrationHearingDate.isEmpty())) {
            currentInterimHearingList.forEach(data -> currentMap.put(String.valueOf(data.getId()), String.join("#",
                data.getValue().getInterimHearingDate(), data.getValue().getInterimHearingTime())));

            log.info("Non collection hearing date {} and time {}",beforeMigrationHearingDate, beforeMigrationHearingTime);
            if (beforeMigrationHearingDate.isEmpty() && beforeMigrationHearingTime.isEmpty()) {
                beforeInterimHearingList.forEach(data -> beforeMap.put(String.valueOf(data.getId()), String.join("#",
                    data.getValue().getInterimHearingDate(), data.getValue().getInterimHearingTime())));
            } else {
                currentInterimHearingList.forEach(data -> beforeMap.put(String.valueOf(data.getId()), String.join("#",
                    beforeMigrationHearingDate, beforeMigrationHearingTime)));
            }
            log.info("beforeMap::" + beforeMap.size());
            currentMap.entrySet().forEach(currentData -> beforeMap.entrySet()
                    .forEach(beforeData -> setList(currentData, beforeData, modifiedCollectionList)));
        }

        log.info("Modified collection list::" + modifiedCollectionList);
        return modifiedCollectionList;
    }

    private  void setList(Map.Entry<String, String> currentDataMap, Map.Entry<String, String> beforeDataMap,
                          List<String> modifiedCollectionList) {
        if (currentDataMap.getKey().equals(beforeDataMap.getKey()) && ! currentDataMap.getValue().equals(beforeDataMap.getValue())) {
            modifiedCollectionList.add(currentDataMap.getKey());
        }
    }

    private List<InterimHearingCollection> sortEarliestHearingFirst(FinremCaseData caseData) {
        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());
        return interimHearingList.stream()
            .sorted(Comparator.nullsLast(Comparator.comparing(e -> e.getValue().getInterimHearingDate())))
            .collect(Collectors.toList());
    }

    private void removeNonCollectionInterimData(FinremCaseData caseData) {
        caseData.getInterimWrapper().setInterimHearingType(null);
        caseData.getInterimWrapper().setInterimHearingDate(null);
        caseData.getInterimWrapper().setInterimHearingTime(null);
        caseData.getInterimWrapper().setInterimTimeEstimate(null);
        caseData.getRegionWrapper().setInterimRegionWrapper(null);
        caseData.getInterimWrapper().setInterimAdditionalInformationAboutHearing(null);
        caseData.getInterimWrapper().setInterimPromptForAnyDocument(null);
        caseData.getInterimWrapper().setInterimUploadAdditionalDocument(null);
        caseData.getInterimWrapper().setInterimHearingDirectionsDocument(null);
    }

    public void sendNotification(FinremCaseDetails caseDetails, FinremCaseDetails caseDetailsBefore) {
        log.info("Sending email notification for case id {}", caseDetails.getId());
        FinremCaseData caseData =  caseDetails.getCaseData();
        FinremCaseData caseDataBefore =  caseDetailsBefore.getCaseData();

        if (!caseData.isPaperCase()) {
            List<InterimHearingCollection> interimHearings = filterInterimHearingToProcess(caseData, caseDataBefore);
            interimHearings.forEach(interimHearingData -> notify(caseDetails, interimHearingData));
        }
    }

    private void notify(FinremCaseDetails caseDetails, InterimHearingCollection interimHearingData) {
        if (caseDetails.getCaseData().isApplicantSolicitorAgreeToReceiveEmails()) {
            log.info("Sending email notification to Applicant Solicitor about interim hearing");
            notificationService.sendInterimHearingNotificationEmailToApplicantSolicitor(caseDetails, interimHearingData);
        }
        if (notificationService.shouldEmailRespondentSolicitor(caseDetails.getCaseData())) {
            log.info("Sending email notification to Respondent Solicitor about interim hearing");
            notificationService.sendInterimHearingNotificationEmailToRespondentSolicitor(caseDetails, interimHearingData);
        }
    }
}
