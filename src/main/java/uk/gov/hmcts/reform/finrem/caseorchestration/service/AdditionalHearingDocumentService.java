package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.CourtDetailsParseException;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ADDITIONAL_HEARING_DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullApplicantName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.buildFullRespondentName;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.nullToEmpty;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdditionalHearingDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final BulkPrintService bulkPrintService;

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

    public void sendAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails) {
        bulkPrintAdditionalHearingDocuments(caseDetails, authorisationToken);
    }

    public void createAndStoreAdditionalHearingDocuments(String authorisationToken, CaseDetails caseDetails)
        throws CourtDetailsParseException, JsonProcessingException {
        List<DirectionDetailsCollectionData> directionDetailsCollectionList = documentHelper
            .convertToDirectionDetailsCollectionData(caseDetails
                .getData()
                .get(DIRECTION_DETAILS_COLLECTION_CT));

        if (directionDetailsCollectionList.size() == 0
            || !YES_VALUE.equalsIgnoreCase(nullToEmpty(
                directionDetailsCollectionList.get(0).getDirectionDetailsCollection().getIsAnotherHearingYN()))) {
            log.info("Additional hearing document not required for case: {}", caseDetails.getId());
            return;
        }

        if (documentHelper.getHearingOrderDocuments(caseDetails.getData()).get(0).getHearingOrderDocuments() != null) {
            caseDetails.getData().put(LATEST_DRAFT_HEARING_ORDER,
                documentHelper.getHearingOrderDocuments(caseDetails.getData()).get(0).getHearingOrderDocuments());
        }

        DirectionDetailsCollection directionDetailsCollection = directionDetailsCollectionList.get(0).getDirectionDetailsCollection();

        Map<String, Object> courtData = directionDetailsCollection.getLocalCourt();
        Map<String, Object> courtDetailsMap;

        courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);

        Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(
            courtData.get(CaseHearingFunctions.getSelectedCourtComplexType(courtData)));

        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        prepareHearingCaseDetails(caseDetailsCopy, courtDetails, directionDetailsCollection.getTypeOfHearing(),
            directionDetailsCollection.getDateOfHearing(), directionDetailsCollection.getHearingTime(), directionDetailsCollection.getTimeEstimate());

        CaseDocument document = generateAdditionalHearingDocument(caseDetailsCopy, authorisationToken);
        addAdditionalHearingDocumentToCaseData(caseDetails, document);
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
        caseData.put("AdditionalHearingDated", new Date());

        caseData.put("CourtName", selectedFRCDetails.getCourtName());
        caseData.put("CourtAddress", selectedFRCDetails.getCourtAddress());
        caseData.put("CourtPhone", selectedFRCDetails.getPhoneNumber());
        caseData.put("CourtEmail", selectedFRCDetails.getEmail());

        caseData.put("CCDCaseNumber", caseDetails.getId());
        caseData.put("DivorceCaseNumber", caseData.get(DIVORCE_CASE_NUMBER));
        caseData.put("ApplicantName", buildFullApplicantName(caseDetails));
        caseData.put("RespondentName", buildFullRespondentName(caseDetails));
    }

    private void addAdditionalHearingDocumentToCaseData(CaseDetails caseDetails, CaseDocument document) {
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

    private void bulkPrintAdditionalHearingDocuments(CaseDetails caseDetails, String authorisationToken) {
        List<AdditionalHearingDocumentData> additionalHearingDocumentData =
            documentHelper.convertToAdditionalHearingDocumentData(
                caseDetails.getData().get(ADDITIONAL_HEARING_DOCUMENT_COLLECTION));

        AdditionalHearingDocumentData additionalHearingDocument = additionalHearingDocumentData.get(additionalHearingDocumentData.size() - 1);

        List<BulkPrintDocument> document = singletonList(bulkPrintService.getBulkPrintDocumentFromCaseDocument(
                additionalHearingDocument.getAdditionalHearingDocument().getDocument()));

        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, document);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, document);
    }
}

