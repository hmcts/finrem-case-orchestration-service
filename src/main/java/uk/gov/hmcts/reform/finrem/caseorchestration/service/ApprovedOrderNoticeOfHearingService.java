package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.CourtDetailsTemplateFields;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.ApprovedOrderNoticeOfHearingCorresponder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;
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
    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final ObjectMapper objectMapper;
    private final ApprovedOrderNoticeOfHearingCorresponder approvedOrderNoticeOfHearingCorresponder;


    public void createAndStoreHearingNoticeDocumentPack(FinremCaseDetails caseDetails,
                                                        String authToken) {

        List<DocumentCollection> hearingNoticePack = new ArrayList<>();
        CaseDocument noticeOfHearingDocument = prepareHearingRequiredNoticeDocumentComplexType(caseDetails, authToken);
        DocumentCollection noticeOfHearingDocumentCollection = getDocumentCollectionObj(
            CaseDocument.builder()
                .documentUrl(noticeOfHearingDocument.getDocumentUrl())
                .documentFilename(noticeOfHearingDocument.getDocumentFilename())
                .documentBinaryUrl(noticeOfHearingDocument.getDocumentBinaryUrl())
                .categoryId(DocumentCategory.SYSTEM_DUPLICATES.getDocumentCategoryId())
                .build()
        );

        hearingNoticePack.add(noticeOfHearingDocumentCollection);

        FinremCaseData caseData = caseDetails.getData();
        List<DocumentCollection> documentCollections = Optional.ofNullable(caseData.getHearingNoticesDocumentCollection()).orElse(new ArrayList<>());
        documentCollections.add(DocumentCollection.builder().value(noticeOfHearingDocument).build());

        documentCollections.forEach(docColl -> addAdditionalHearingDocument(caseData, docColl.getValue()));

        Optional<CaseDocument> latestDraftHearingOrder = Optional.ofNullable(caseData.getLatestDraftHearingOrder());
        if (latestDraftHearingOrder.isPresent()) {
            hearingNoticePack.add(getDocumentCollectionObj(caseData.getLatestDraftHearingOrder()));
        }

        caseData.setHearingNoticeDocumentPack(hearingNoticePack);
    }


    private DocumentCollection getDocumentCollectionObj(CaseDocument caseDocument) {
        return DocumentCollection.builder().value(caseDocument).build();
    }

    private void addAdditionalHearingDocument(FinremCaseData caseData, CaseDocument documentToAdd) {

        List<AdditionalHearingDocumentCollection> additionalHearingDocuments
            = Optional.ofNullable(caseData.getAdditionalHearingDocuments()).orElse(new ArrayList<>());

        AdditionalHearingDocumentCollection hearingDocumentCollection
            = AdditionalHearingDocumentCollection.builder()
            .value(AdditionalHearingDocument.builder().document(documentToAdd).build()).build();
        additionalHearingDocuments.add(hearingDocumentCollection);

        caseData.setAdditionalHearingDocuments(additionalHearingDocuments);
    }

    public void printHearingNoticePackAndSendToApplicantAndRespondent(CaseDetails caseDetails,
                                                                      String authorisationToken) {
        approvedOrderNoticeOfHearingCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }

    private CaseDocument prepareHearingRequiredNoticeDocumentComplexType(FinremCaseDetails caseDetails, String authorisationToken) {
        FinremCaseData data = caseDetails.getData();
        Optional<List<HearingDirectionDetailsCollection>> hearingDirectionDetailsCollection
            = Optional.ofNullable(data.getHearingDirectionDetailsCollection());

        if (hearingDirectionDetailsCollection.isEmpty()) {
            throw new IllegalStateException("Invalid Case Data - hearing direction is empty for caseId " + caseDetails.getId());
        }
        List<HearingDirectionDetailsCollection> hearingDirectionDetailsCollections = hearingDirectionDetailsCollection.get();
        HearingDirectionDetailsCollection directionDetailsCollection
            = hearingDirectionDetailsCollections.get(hearingDirectionDetailsCollections.size() - 1);
        HearingDirectionDetail directionDetail = directionDetailsCollection.getValue();

        Map<String, Object> mapOfLetterDetails = getNoticeOfHearingLetterDetails(caseDetails, directionDetail);

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, mapOfLetterDetails,
            documentConfiguration.getAdditionalHearingTemplate(),
            documentConfiguration.getAdditionalHearingFileName(), caseDetails.getId().toString());

    }


    private Map<String, Object> getNoticeOfHearingLetterDetails(FinremCaseDetails caseDetails,
                                                                HearingDirectionDetail directionDetail) {
        CourtDetailsTemplateFields selectedFRCDetails = getFrcCourtDetails(directionDetail);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_DATA, addValuesToPlaceHoldersMap(caseDetails, directionDetail, selectedFRCDetails));
        caseDataMap.put("id", caseDetails.getId());
        HashMap<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put(CASE_DETAILS, caseDataMap);
        return caseDetailsMap;
    }

    private CourtDetailsTemplateFields getFrcCourtDetails(HearingDirectionDetail directionDetail) {
        Map<String, Object> courtDetails = getCourtDetails(directionDetail);
        return CourtDetailsTemplateFields.builder()
            .courtName(nullToEmpty(courtDetails.get(COURT_DETAILS_NAME_KEY)))
            .courtAddress(nullToEmpty(courtDetails.get(COURT_DETAILS_ADDRESS_KEY)))
            .phoneNumber(nullToEmpty(courtDetails.get(COURT_DETAILS_PHONE_KEY)))
            .email(nullToEmpty(courtDetails.get(COURT_DETAILS_EMAIL_KEY)))
            .build();
    }

    private Map<String, Object> getCourtDetails(HearingDirectionDetail directionDetail) {

        try {
            Map<String, Object> listOfCourtDetails = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> hearingCourtMap = convertToMap(directionDetail.getLocalCourt());
            String selectedCourtKey = getSelectedCourtComplexType(hearingCourtMap);
            return (Map<String, Object>) listOfCourtDetails.get(hearingCourtMap.get(selectedCourtKey));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, Object> convertToMap(Object object) {
        return objectMapper.convertValue(object, new TypeReference<>() {
        });
    }

    private Map<String, Object> addValuesToPlaceHoldersMap(FinremCaseDetails caseDetails,
                                                           HearingDirectionDetail directionDetail,
                                                           CourtDetailsTemplateFields selectedFRCDetails) {
        Map<String, Object> placeholdersMap = new HashMap<>();
        placeholdersMap.put("HearingType", directionDetail.getTypeOfHearing());
        placeholdersMap.put("HearingVenue", getFrcCourtDetailsAsOneLineAddressString(getCourtDetails(directionDetail)));
        placeholdersMap.put("HearingDate", directionDetail.getDateOfHearing());
        placeholdersMap.put("HearingTime", directionDetail.getHearingTime());
        placeholdersMap.put("HearingLength", directionDetail.getTimeEstimate());
        placeholdersMap.put("AdditionalHearingDated", DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT).format(LocalDate.now()));

        placeholdersMap.put("CourtName", selectedFRCDetails.getCourtName());
        placeholdersMap.put("CourtAddress", selectedFRCDetails.getCourtAddress());
        placeholdersMap.put("CourtPhone", selectedFRCDetails.getPhoneNumber());
        placeholdersMap.put("CourtEmail", selectedFRCDetails.getEmail());

        placeholdersMap.put("CCDCaseNumber", caseDetails.getId());
        placeholdersMap.put("DivorceCaseNumber", nullToEmpty(caseDetails.getData().getDivorceCaseNumber()));
        placeholdersMap.put("ApplicantName", caseDetails.getData().getFullApplicantName());
        placeholdersMap.put("RespondentName", caseDetails.getData().getRespondentFullName());

        return placeholdersMap;
    }
}
