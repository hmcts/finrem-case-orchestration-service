package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.NoticeOfHearingLetterDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ANOTHER_HEARING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtComplexType;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovedOrderNoticeOfHearingService {

    static final String CASE_DETAILS = "caseDetails";
    static final String CASE_DATA = "case_data";

    private final DocumentHelper documentHelper;
    private final GenericDocumentService genericDocumentService;
    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final ObjectMapper objectMapper;

    public void submitNoticeOfHearing(CaseDetails caseDetails, String authorisationToken) {
        Optional<List<BulkPrintDocument>> documentsOptional =
            prepareHearingRequiredNoticeDocumentsForPrint(caseDetails, authorisationToken);

        documentsOptional.ifPresent(documents ->
            printDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, documents));
    }

    private Optional<List<BulkPrintDocument>> prepareHearingRequiredNoticeDocumentsForPrint(CaseDetails caseDetails,
                                                                                            String authToken) {
        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> documents = new ArrayList<>();
        Optional<CaseDocument> directionsDocument = caseData.get(ANOTHER_HEARING_TO_BE_LISTED).equals(YES_VALUE)
            ? Optional.of(prepareHearingRequiredNoticeDocumentComplexType(caseDetails, authToken))
            : Optional.empty();

        if (directionsDocument.isEmpty()) {
            return Optional.empty();
        }
        documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(directionsDocument.get()));
        caseData.put(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT, directionsDocument.get());
        return Optional.of(documents);
    }

    private CaseDocument prepareHearingRequiredNoticeDocumentComplexType(CaseDetails caseDetails, String authorisationToken) {
        NoticeOfHearingLetterDetails noticeOfHearingLetterDetails = NoticeOfHearingLetterDetails.builder()
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .ccdCaseNumber(caseDetails.getId())
            .letterDate(String.valueOf(LocalDate.now()))
            .build();

        addHearingVenueDetailsFromDirectionDetailsCollection(caseDetails, noticeOfHearingLetterDetails);
        Map<String, Object> mapOfLetterDetails = convertLetterDetailsToMap(noticeOfHearingLetterDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, mapOfLetterDetails,
            documentConfiguration.getGeneralApplicationHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationHearingNoticeFileName());
    }

    private void addHearingVenueDetailsFromDirectionDetailsCollection(CaseDetails caseDetails,
                                                                      NoticeOfHearingLetterDetails noticeOfHearingLetterDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        Optional<DirectionDetailsCollection> latestDirectionDetails = getLatestDirectionDetails(caseData);
        if (latestDirectionDetails.isPresent()) {
            Map<String, Object> courtDetails = getCourtDetailsFromLatestDirectionDetailsItem(latestDirectionDetails.get());
            noticeOfHearingLetterDetails.setHearingVenue(getFrcCourtDetailsAsOneLineAddressString(courtDetails));
            noticeOfHearingLetterDetails.setCourtDetails(courtDetails);
            noticeOfHearingLetterDetails.setGeneralApplicationDirectionsHearingDate(
                latestDirectionDetails.get().getDateOfHearing());
            noticeOfHearingLetterDetails.setGeneralApplicationDirectionsHearingTime(
                latestDirectionDetails.get().getHearingTime());
            noticeOfHearingLetterDetails.setGeneralApplicationDirectionsHearingTimeEstimate(
                latestDirectionDetails.get().getTimeEstimate());
        }
    }

    private Map<String, Object> getCourtDetailsFromLatestDirectionDetailsItem(DirectionDetailsCollection latestDirectionDetails) {
        try {
            Map<String, Object> listOfCourtDetails = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> hearingCourtMap = latestDirectionDetails.getLocalCourt();
            return (Map<String, Object>) listOfCourtDetails.get(hearingCourtMap.get(getSelectedCourtComplexType(hearingCourtMap)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<DirectionDetailsCollection> getLatestDirectionDetails(Map<String, Object> caseData) {
        List<DirectionDetailsCollectionData> directionDetailsCollectionList =
            documentHelper.convertToDirectionDetailsCollectionData(caseData.get(DIRECTION_DETAILS_COLLECTION_CT));

        return Optional.ofNullable(directionDetailsCollectionList).isEmpty()
            ? Optional.empty()
            : Optional.of(directionDetailsCollectionList.get(directionDetailsCollectionList.size() - 1)
            .getDirectionDetailsCollection());
    }

    private void printDocumentPackAndSendToApplicantAndRespondent(CaseDetails caseDetails, String authorisationToken,
                                                                  List<BulkPrintDocument> documents) {
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
    }

    private Map convertLetterDetailsToMap(NoticeOfHearingLetterDetails noticeOfHearingLetterDetails) {
        HashMap caseDetailsMap = new HashMap<String, Object>();
        HashMap caseDataMap = new HashMap<String, Object>();
        caseDataMap.put(CASE_DATA, objectMapper.convertValue(noticeOfHearingLetterDetails, Map.class));
        caseDataMap.put("id", noticeOfHearingLetterDetails.getCcdCaseNumber());
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }
}
