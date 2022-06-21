package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.NoticeOfHearingLetterDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ANOTHER_HEARING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;
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
            caseData.put(ADDITIONAL_HEARING_DOCUMENT_COLLECTION, hearingNoticeDocuments);
            Optional.ofNullable(documentHelper.convertToCaseDocument(caseDetails.getData().get(LATEST_DRAFT_HEARING_ORDER)))
                .ifPresent(directionsDocuments::add);
        }
        return documentHelper.getCaseDocumentsAsBulkPrintDocuments(directionsDocuments);
    }

    private CaseDocument prepareHearingRequiredNoticeDocumentComplexType(CaseDetails caseDetails, String authorisationToken) {
        Optional<AdditionalHearingDirectionsCollection> latestAdditionalHearingDirections =
            getLatestAdditionalHearingDirections(caseDetails);

        if (latestAdditionalHearingDirections.isEmpty()) {
            throw new IllegalStateException("Invalid Case Data");
        }
        NoticeOfHearingLetterDetails noticeOfHearingLetterDetails =
            getNoticeOfHearingLetterDetails(caseDetails, latestAdditionalHearingDirections.get());
        Map<String, Object> mapOfLetterDetails = convertLetterDetailsToMap(noticeOfHearingLetterDetails);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, mapOfLetterDetails,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
    }

    private void printDocumentPackAndSendToApplicantAndRespondent(CaseDetails caseDetails, String authorisationToken,
                                                                  List<BulkPrintDocument> documents) {
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
    }

    private NoticeOfHearingLetterDetails getNoticeOfHearingLetterDetails(CaseDetails caseDetails,
                                                                         AdditionalHearingDirectionsCollection
                                                                             additionalHearingDirectionsCollection) {
        Map<String, Object> caseData = caseDetails.getData();
        Map<String, Object> courtDetails = getCourtDetails(additionalHearingDirectionsCollection);
        return NoticeOfHearingLetterDetails.builder()
            .applicantName(documentHelper.getApplicantFullName(caseDetails))
            .respondentName(documentHelper.getRespondentFullNameContested(caseDetails))
            .divorceCaseNumber(nullToEmpty(caseData.get(DIVORCE_CASE_NUMBER)))
            .ccdCaseNumber(caseDetails.getId())
            .courtDetails(courtDetails)
            .hearingVenue(getFrcCourtDetailsAsOneLineAddressString(courtDetails))
            .HearingDate(additionalHearingDirectionsCollection.getDateOfHearing())
            .HearingTime(additionalHearingDirectionsCollection.getHearingTime())
            .HearingLength(additionalHearingDirectionsCollection.getTimeEstimate())
            .generalApplicationDirectionsHearingInformation(additionalHearingDirectionsCollection.getAnyOtherListingInstructions())
            .HearingType(additionalHearingDirectionsCollection.getTypeOfHearing())
            .letterDate(String.valueOf(LocalDate.now()))
            .build();
    }

    private Map<String, Object> getCourtDetails(AdditionalHearingDirectionsCollection latestAdditionalHearingDirection) {

        try {
            Map listOfCourtDetails = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map hearingCourtMap = latestAdditionalHearingDirection.getLocalCourt();
            String selectedCourtKey = getSelectedCourtComplexType(hearingCourtMap);
            return  (Map<String, Object>) listOfCourtDetails.get(hearingCourtMap.get(selectedCourtKey));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<AdditionalHearingDirectionsCollection> getLatestAdditionalHearingDirections(CaseDetails caseDetails) {
        List<Element<AdditionalHearingDirectionsCollection>> additionalHearingDetailsCollection =
            objectMapper.convertValue(caseDetails.getData().get(HEARING_DIRECTION_DETAILS_COLLECTION),
                new TypeReference<>() {});

        return additionalHearingDetailsCollection != null && !additionalHearingDetailsCollection.isEmpty()
            ? Optional.of(additionalHearingDetailsCollection.get(additionalHearingDetailsCollection.size() - 1).getValue())
            : Optional.empty();
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
