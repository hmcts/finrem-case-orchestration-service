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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICES_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
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
        List<BulkPrintDocument> noticeOfHearingDocsToPrint =
            prepareHearingRequiredNoticeDocumentsForPrint(caseDetails, authorisationToken);
        if (!noticeOfHearingDocsToPrint.isEmpty()) {
            printDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, noticeOfHearingDocsToPrint);
        }
    }

    private List<BulkPrintDocument> prepareHearingRequiredNoticeDocumentsForPrint(CaseDetails caseDetails,
                                                                                  String authToken) {
        Map<String, Object> caseData = caseDetails.getData();
        List<CaseDocument> directionsDocuments = new ArrayList<>();
        if (caseData.get(ANOTHER_HEARING_TO_BE_LISTED).equals(YES_VALUE)) {
            CaseDocument noticeOfHearingDocument = prepareHearingRequiredNoticeDocumentComplexType(caseDetails, authToken);
            directionsDocuments.add(noticeOfHearingDocument);
            List<CaseDocument> hearingNoticeDocuments = Optional.ofNullable(documentHelper.getHearingNoticeDocuments(caseData))
                .orElse(new ArrayList<>());
            hearingNoticeDocuments.add(noticeOfHearingDocument);
            caseData.put(HEARING_NOTICES_COLLECTION, hearingNoticeDocuments);
            Optional.ofNullable((CaseDocument) caseDetails.getData().get(LATEST_DRAFT_HEARING_ORDER))
                .map(latestDraftHearingOrder -> directionsDocuments.add(latestDraftHearingOrder));
        }
        return documentHelper.getCaseDocumentsAsBulkPrintDocuments(directionsDocuments);
    }

    private CaseDocument prepareHearingRequiredNoticeDocumentComplexType(CaseDetails caseDetails, String authorisationToken) {
        NoticeOfHearingLetterDetails noticeOfHearingLetterDetails = NoticeOfHearingLetterDetails.builder()
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .divorceCaseNumber(nullToEmpty(caseDetails.getData().get(DIVORCE_CASE_NUMBER)))
            .ccdCaseNumber(caseDetails.getId())
            .letterDate(String.valueOf(LocalDate.now()))
            .build();

        addHearingVenueDetailsFromDirectionDetailsCollection(caseDetails, noticeOfHearingLetterDetails);
        Map<String, Object> mapOfLetterDetails = convertLetterDetailsToMap(noticeOfHearingLetterDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, mapOfLetterDetails,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
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
