package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.additionalhearing.AdditionalHearingDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.ccd.domain.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionDetailCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.DirectionOrderCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Collections.singletonList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdditionalHearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final BulkPrintService bulkPrintService;
    private final AdditionalHearingDetailsMapper additionalHearingDetailsMapper;
    private final CaseDataService caseDataService;
    private final NotificationService notificationService;


    public void createAdditionalHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        FinremCaseData caseData = caseDetails.getCaseData();

        Map<String, Object> additionalHearingPlaceholdersMap = additionalHearingDetailsMapper
            .getDocumentTemplateDetailsAsMap(caseDetails, caseData.getRegionWrapper().getDefaultCourtList());

        Document document = generateAdditionalHearingDocument(additionalHearingPlaceholdersMap, authorisationToken);
        addAdditionalHearingDocumentToCaseData(caseDetails, document);
    }

    public void sendAdditionalHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails) {
        bulkPrintAdditionalHearingDocuments(caseDetails, authorisationToken);
    }

    public void createAndStoreAdditionalHearingDocuments(String authorisationToken, FinremCaseDetails caseDetails)
        throws CourtDetailsParseException, JsonProcessingException {

        List<DirectionOrderCollection> hearingOrderCollectionData = caseDetails.getCaseData().getUploadHearingOrder();

        if (hearingOrderCollectionData != null
            && !hearingOrderCollectionData.isEmpty()
            && hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1).getValue() != null) {

            caseDetails.getCaseData().setLatestDraftHearingOrder(hearingOrderCollectionData.get(
                hearingOrderCollectionData.size() - 1).getValue().getUploadDraftDocument());
        }

        List<DirectionDetailCollection> directionDetailsCollectionList = caseDetails.getCaseData()
            .getDirectionDetailsCollection();

        //check that the list contains one or more values for the court hearing information
        if (!directionDetailsCollectionList.isEmpty()) {
            DirectionDetail latestDirectionDetailsCollectionItem =
                directionDetailsCollectionList.get(directionDetailsCollectionList.size() - 1).getValue();

            //if the latest court hearing has specified another hearing as No, dont create an additional hearing document
            if (latestDirectionDetailsCollectionItem.getIsAnotherHearingYN().isNoOrNull()) {
                log.info("Additional hearing document not required for case: {}", caseDetails.getId());
                return;
            }

            //Otherwise, proceed to extract data from collection
            //Generate and store new additional hearing document using latestDirectionDetailsCollectionItem
            FinremCaseDetails caseDetailsCopy = getLatestHearingCaseDetailsVersion(caseDetails, latestDirectionDetailsCollectionItem);
            Map<String, Object> additionalHearingPlaceholdersMap = additionalHearingDetailsMapper
                .getDocumentTemplateDetailsAsMap(caseDetailsCopy, latestDirectionDetailsCollectionItem.getLocalCourt());

            Document document = generateAdditionalHearingDocument(additionalHearingPlaceholdersMap, authorisationToken);
            addAdditionalHearingDocumentToCaseData(caseDetails, document);
        } else {
            log.info("Additional hearing document not required for case: {}", caseDetails.getId());
        }
    }

    private Document generateAdditionalHearingDocument(Map<String, Object> placeholdersMap, String authorisationToken) {
        log.info("Generating Additional Hearing Document for Case ID: {}", placeholdersMap.get("ccdCaseNumber"));

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, placeholdersMap,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
    }

    public void addAdditionalHearingDocumentToCaseData(FinremCaseDetails caseDetails, Document document) {
        AdditionalHearingDocumentCollection generatedDocumentData = AdditionalHearingDocumentCollection.builder()
            .value(uk.gov.hmcts.reform.finrem.ccd.domain.AdditionalHearingDocument.builder()
                .additionalHearingDocument(document)
                .build())
            .build();

        FinremCaseData caseData = caseDetails.getCaseData();
        List<AdditionalHearingDocumentCollection> additionalHearingDocumentDataList =
            Optional.ofNullable(caseData.getAdditionalHearingDocuments())
                .orElse(new ArrayList<>(1));

        additionalHearingDocumentDataList.add(generatedDocumentData);

        caseData.setAdditionalHearingDocuments(additionalHearingDocumentDataList);
    }

    public void bulkPrintAdditionalHearingDocuments(FinremCaseDetails caseDetails, String authorisationToken) {
        List<AdditionalHearingDocumentCollection> additionalHearingDocumentData = Optional.ofNullable(caseDetails.getCaseData()
            .getAdditionalHearingDocuments()).orElse(new ArrayList<>());

        AdditionalHearingDocumentCollection additionalHearingDocument =
            additionalHearingDocumentData.get(additionalHearingDocumentData.size() - 1);

        List<BulkPrintDocument> document = singletonList(documentHelper.getDocumentAsBulkPrintDocument(
            additionalHearingDocument.getValue().getAdditionalHearingDocument()).orElseThrow());

        if (!notificationService.isContestedApplicantSolicitorEmailCommunicationEnabled(caseDetails.getData())) {
            CompletableFuture.runAsync(() ->
                bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, document));
        }
        if (!notificationService.isRespondentSolicitorEmailCommunicationEnabled(caseDetails.getData())) {
            CompletableFuture.runAsync(() ->
                bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, document));
        }
    }

    private FinremCaseDetails getLatestHearingCaseDetailsVersion(FinremCaseDetails caseDetails,
                                                                 DirectionDetail latestDirectionDetailsCollectionItem) {
        FinremCaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, FinremCaseDetails.class);
        caseDetailsCopy.getCaseData().setHearingDate(latestDirectionDetailsCollectionItem.getDateOfHearing());
        caseDetailsCopy.getCaseData().setHearingType(latestDirectionDetailsCollectionItem.getTypeOfHearing());
        caseDetailsCopy.getCaseData().setHearingTime(latestDirectionDetailsCollectionItem.getHearingTime());
        caseDetailsCopy.getCaseData().setTimeEstimate(latestDirectionDetailsCollectionItem.getTimeEstimate());

        return caseDetailsCopy;
    }

    private void convertHearingOrderCollectionDocumentsToPdf(DirectionOrderCollection element,
                                                             String authorisationToken) {
        Document pdfApprovedOrder = genericDocumentService.convertDocumentIfNotPdfAlready(
            element.getValue().getUploadDraftDocument(), authorisationToken);
        element.getValue().setUploadDraftDocument(pdfApprovedOrder);
    }

    public void createAndStoreAdditionalHearingDocumentsFromApprovedOrder(String authorisationToken, FinremCaseDetails caseDetails) {
        List<DirectionOrderCollection> hearingOrderCollectionData = caseDetails.getCaseData().getUploadHearingOrder();

        if (hearingOrderCollectionHasEntries(hearingOrderCollectionData)) {
            populateLatestDraftHearingOrderWithLatestEntry(caseDetails, hearingOrderCollectionData, authorisationToken);
        }
    }

    private boolean hearingOrderCollectionHasEntries(List<DirectionOrderCollection> hearingOrderCollectionData) {
        return hearingOrderCollectionData != null
            && !hearingOrderCollectionData.isEmpty()
            && hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1).getValue() != null;
    }

    private void populateLatestDraftHearingOrderWithLatestEntry(FinremCaseDetails caseDetails,
                                                                List<DirectionOrderCollection> hearingOrderCollectionData,
                                                                String authorisationToken) {
        hearingOrderCollectionData.forEach(element -> convertHearingOrderCollectionDocumentsToPdf(element, authorisationToken));
        caseDetails.getCaseData().setUploadHearingOrder(hearingOrderCollectionData);
        caseDetails.getCaseData().setLatestDraftHearingOrder(hearingOrderCollectionData.get(hearingOrderCollectionData.size() - 1)
            .getValue().getUploadDraftDocument());
    }
}
