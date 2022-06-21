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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;
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

        CaseDocument noticeOfHearingDocument = prepareHearingRequiredNoticeDocumentComplexType(caseDetails, authToken);
        directionsDocuments.add(noticeOfHearingDocument);
        List<CaseDocument> hearingNoticeDocuments = Optional.ofNullable(documentHelper.getHearingNoticeDocuments(caseData))
            .orElse(new ArrayList<>());
        hearingNoticeDocuments.add(noticeOfHearingDocument);
        caseData.put(ADDITIONAL_HEARING_DOCUMENT_COLLECTION, hearingNoticeDocuments);
        Optional.ofNullable(documentHelper.convertToCaseDocument(caseDetails.getData().get(LATEST_DRAFT_HEARING_ORDER)))
            .ifPresent(directionsDocuments::add);

        return documentHelper.getCaseDocumentsAsBulkPrintDocuments(directionsDocuments);
    }

    private CaseDocument prepareHearingRequiredNoticeDocumentComplexType(CaseDetails caseDetails, String authorisationToken) {
        Optional<AdditionalHearingDirectionsCollection> latestAdditionalHearingDirections =
            getLatestAdditionalHearingDirections(caseDetails);

        if (latestAdditionalHearingDirections.isEmpty()) {
            throw new IllegalStateException("Invalid Case Data");
        }
        Map<String, Object> mapOfLetterDetails = getNoticeOfHearingLetterDetails(caseDetails,
            latestAdditionalHearingDirections.get());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, mapOfLetterDetails,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName());
    }

    private void printDocumentPackAndSendToApplicantAndRespondent(CaseDetails caseDetails, String authorisationToken,
                                                                  List<BulkPrintDocument> documents) {
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
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

    private Map getNoticeOfHearingLetterDetails(CaseDetails caseDetails,
                                          AdditionalHearingDirectionsCollection additionalHearingDirectionsCollection) {
        FrcCourtDetails selectedFRCDetails = getFrcCourtDetails(additionalHearingDirectionsCollection);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_DATA, addValuesToPlaceHoldersMap(caseDetails, additionalHearingDirectionsCollection, selectedFRCDetails));
        caseDataMap.put("id", caseDetails.getId());
        HashMap caseDetailsMap = new HashMap<String, Object>();
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }

    private FrcCourtDetails getFrcCourtDetails(AdditionalHearingDirectionsCollection additionalHearingDirectionsCollection) {
        Map<String, Object> courtDetails = getCourtDetails(additionalHearingDirectionsCollection);
        return FrcCourtDetails.builder()
            .courtName((String) courtDetails.get(COURT_DETAILS_NAME_KEY))
            .courtAddress((String) courtDetails.get(COURT_DETAILS_ADDRESS_KEY))
            .phoneNumber((String) courtDetails.get(COURT_DETAILS_PHONE_KEY))
            .email((String) courtDetails.get(COURT_DETAILS_EMAIL_KEY))
            .build();
    }

    private Map<String, Object> addValuesToPlaceHoldersMap(CaseDetails caseDetails,
                                            AdditionalHearingDirectionsCollection additionalHearingDirectionsCollection,
                                            FrcCourtDetails selectedFRCDetails) {
        Map<String, Object> placeholdersMap = new HashMap<>();
        placeholdersMap.put("HearingType", additionalHearingDirectionsCollection.getTypeOfHearing());
        placeholdersMap.put("HearingVenue", getFrcCourtDetailsAsOneLineAddressString(getCourtDetails(additionalHearingDirectionsCollection)));
        placeholdersMap.put("HearingDate", additionalHearingDirectionsCollection.getDateOfHearing());
        placeholdersMap.put("HearingTime", additionalHearingDirectionsCollection.getHearingTime());
        placeholdersMap.put("HearingLength", additionalHearingDirectionsCollection.getTimeEstimate());
        placeholdersMap.put("AdditionalHearingDated", new Date());

        placeholdersMap.put("CourtName", selectedFRCDetails.getCourtName());
        placeholdersMap.put("CourtAddress", selectedFRCDetails.getCourtAddress());
        placeholdersMap.put("CourtPhone", selectedFRCDetails.getPhoneNumber());
        placeholdersMap.put("CourtEmail", selectedFRCDetails.getEmail());
        placeholdersMap.put("AnyOtherDirections",
            additionalHearingDirectionsCollection.getAnyOtherListingInstructions());

        placeholdersMap.put("CCDCaseNumber", caseDetails.getId());
        placeholdersMap.put("DivorceCaseNumber", nullToEmpty(caseDetails.getData().get(DIVORCE_CASE_NUMBER)));
        placeholdersMap.put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));
        placeholdersMap.put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));

        return placeholdersMap;
    }
}
