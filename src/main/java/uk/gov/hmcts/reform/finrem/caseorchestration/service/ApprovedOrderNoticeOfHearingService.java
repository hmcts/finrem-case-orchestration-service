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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckApplicantSolicitorIsDigitalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.solicitors.CheckRespondentSolicitorIsDigitalService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_ADDRESS_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_EMAIL_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_NAME_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.COURT_DETAILS_PHONE_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_NOTICE_DOCUMENT_PACK;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LATEST_DRAFT_HEARING_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element.element;
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
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final CheckApplicantSolicitorIsDigitalService checkApplicantSolicitorIsDigitalService;
    private final CheckRespondentSolicitorIsDigitalService checkRespondentSolicitorIsDigitalService;
    private final NotificationService notificationService;
    private final CaseDataService caseDataService;

    public void createAndStoreHearingNoticeDocumentPack(CaseDetails caseDetails,
                                                        String authToken) {
        List<CaseDocument> hearingNoticePack = new ArrayList<>();

        CaseDocument noticeOfHearingDocument = prepareHearingRequiredNoticeDocumentComplexType(caseDetails, authToken);
        hearingNoticePack.add(noticeOfHearingDocument);
        List<CaseDocument> hearingNoticeDocuments = addHearingNoticeToHearingDocumentCollection(noticeOfHearingDocument, caseDetails);
        hearingNoticeDocuments.forEach(document ->
            additionalHearingDocumentService.addAdditionalHearingDocumentToCaseData(caseDetails, document));

        Optional.ofNullable(documentHelper.convertToCaseDocument(caseDetails.getData().get(LATEST_DRAFT_HEARING_ORDER)))
            .ifPresent(hearingNoticePack::add);
        caseDetails.getData().put(HEARING_NOTICE_DOCUMENT_PACK, convertHearingNoticeDocumentPackToCcdCollection(hearingNoticePack));
    }

    public void printHearingNoticePackAndSendToApplicantAndRespondent(CaseDetails caseDetails,
                                                                      String authorisationToken) {
        List<CaseDocument> hearingNoticePack = getHearingNoticeDocumentPackFromCaseData(caseDetails);
        List<BulkPrintDocument> documentsToPrint = documentHelper.getCaseDocumentsAsBulkPrintDocuments(hearingNoticePack);

        notifyApplicant(caseDetails, authorisationToken, documentsToPrint);
        notifyRespondent(caseDetails, authorisationToken, documentsToPrint);
    }

    private void notifyApplicant(CaseDetails caseDetails, String authorisationToken, List<BulkPrintDocument> documentsToPrint) {
        if (checkApplicantSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            && caseDataService.isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {

            notificationService.sendPrepareForHearingEmailApplicant(caseDetails);
            return;
        }
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documentsToPrint);
    }

    private void notifyRespondent(CaseDetails caseDetails, String authorisationToken, List<BulkPrintDocument> documentsToPrint) {
        if (checkRespondentSolicitorIsDigitalService.isSolicitorDigital(caseDetails)
            && caseDataService.isRespondentSolicitorAgreeToReceiveEmails(caseDetails)) {
            notificationService.sendPrepareForHearingEmailRespondent(caseDetails);
        } else {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documentsToPrint);
        }
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
            .courtName(nullToEmpty(courtDetails.get(COURT_DETAILS_NAME_KEY)))
            .courtAddress(nullToEmpty(courtDetails.get(COURT_DETAILS_ADDRESS_KEY)))
            .phoneNumber(nullToEmpty(courtDetails.get(COURT_DETAILS_PHONE_KEY)))
            .email(nullToEmpty(courtDetails.get(COURT_DETAILS_EMAIL_KEY)))
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

        placeholdersMap.put("CCDCaseNumber", caseDetails.getId());
        placeholdersMap.put("DivorceCaseNumber", nullToEmpty(caseDetails.getData().get(DIVORCE_CASE_NUMBER)));
        placeholdersMap.put("ApplicantName", documentHelper.getApplicantFullName(caseDetails));
        placeholdersMap.put("RespondentName", documentHelper.getRespondentFullNameContested(caseDetails));

        return placeholdersMap;
    }

    private List<CaseDocument> addHearingNoticeToHearingDocumentCollection(CaseDocument noticeOfHearingDocument,
                                                                           CaseDetails caseDetails) {
        List<CaseDocument> hearingNoticeDocuments = Optional.ofNullable(documentHelper.getHearingNoticeDocuments(caseDetails.getData()))
            .orElse(new ArrayList<>());
        hearingNoticeDocuments.add(noticeOfHearingDocument);

        return hearingNoticeDocuments;
    }

    private List<Element<CaseDocument>> convertHearingNoticeDocumentPackToCcdCollection(List<CaseDocument> hearingNoticePack) {
        return hearingNoticePack.stream()
            .map(document -> element(UUID.randomUUID(), document))
            .collect(Collectors.toList());
    }

    private List<CaseDocument> getHearingNoticeDocumentPackFromCaseData(CaseDetails caseDetails) {
        List<Element<CaseDocument>> hearingNoticePack = objectMapper.convertValue(
            caseDetails.getData().get(HEARING_NOTICE_DOCUMENT_PACK),
            new TypeReference<>() {});

        return hearingNoticePack.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
    }
}
