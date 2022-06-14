package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DirectionDetailsCollectionData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.PAPER_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ANOTHER_HEARING_TO_BE_LISTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONTESTED_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIRECTION_DETAILS_COLLECTION_CT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CFC_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_RECITALS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_FRC;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DOCUMENT_LATEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DRAFT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_PRE_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERIM_HEARING_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_NOTIFICATIONS_EMAIL_CONSENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildInterimFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtComplexType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtGA;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtIH;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationDirectionsService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    public void startGeneralApplicationDirections(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        Stream.of(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE,
            GENERAL_APPLICATION_DIRECTIONS_HEARING_REGION,
            GENERAL_APPLICATION_DIRECTIONS_LONDON_FRC,
            GENERAL_APPLICATION_DIRECTIONS_MIDLANDS_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_NORTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHEAST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_SOUTHWEST_FRC,
            GENERAL_APPLICATION_DIRECTIONS_WALES_FRC,
            GENERAL_APPLICATION_DIRECTIONS_BEDFORDSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BIRMINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_BRISTOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CFC_COURT,
            GENERAL_APPLICATION_DIRECTIONS_CLEVELAND_COURT,
            GENERAL_APPLICATION_DIRECTIONS_DEVON_COURT,
            GENERAL_APPLICATION_DIRECTIONS_DORSET_COURT,
            GENERAL_APPLICATION_DIRECTIONS_HUMBER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_KENTSURREY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LANCASHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_LIVERPOOL_COURT,
            GENERAL_APPLICATION_DIRECTIONS_MANCHESTER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NEWPORT_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NOTTINGHAM_COURT,
            GENERAL_APPLICATION_DIRECTIONS_NWYORKSHIRE_COURT,
            GENERAL_APPLICATION_DIRECTIONS_SWANSEA_COURT,
            GENERAL_APPLICATION_DIRECTIONS_THAMESVALLEY_COURT,
            GENERAL_APPLICATION_DIRECTIONS_WALES_OTHER_COURT,
            GENERAL_APPLICATION_DIRECTIONS_ADDITIONAL_INFORMATION,
            GENERAL_APPLICATION_DIRECTIONS_COURT_ORDER_DATE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_TYPE,
            GENERAL_APPLICATION_DIRECTIONS_JUDGE_NAME,
            GENERAL_APPLICATION_DIRECTIONS_RECITALS,
            GENERAL_APPLICATION_DIRECTIONS_TEXT_FROM_JUDGE
        ).forEach(generalApplicationDirectionCcdField -> caseData.remove(generalApplicationDirectionCcdField));
    }

    public void submitInterimHearing(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> documents = prepareInterimHearingDocumentsToPrint(caseDetails, authorisationToken);
        printInterimDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, documents);
    }

    private void printInterimDocumentPackAndSendToApplicantAndRespondent(CaseDetails caseDetails, String authorisationToken,
                                                                  List<BulkPrintDocument> documents) {
        Map<String, Object> caseData = caseDetails.getData();
        if (isPaperApplication(caseData) || !isApplicantSolicitorAgreeToReceiveEmails(caseDetails)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        }
        if (isPaperApplication(caseData) || !isRespondentSolicitorAgreeToReceiveEmails(caseData)) {
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
        }
    }

    private List<BulkPrintDocument> prepareInterimHearingDocumentsToPrint(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> documents = new ArrayList<>();
        CaseDocument interimDocument = prepareInterimHearingRequiredNoticeDocument(caseDetails, authorisationToken);
        documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(interimDocument));

        if (!isNull(caseData.get(INTERIM_HEARING_UPLOADED_DOCUMENT))) {
            log.warn("Additional uploaded interim document found for printing for case");
            CaseDocument caseDocument = documentHelper.convertToCaseDocument(caseData.get(INTERIM_HEARING_UPLOADED_DOCUMENT));
            CaseDocument additionalUploadedDocuments = genericDocumentService.convertDocumentIfNotPdfAlready(caseDocument, authorisationToken);
            documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(additionalUploadedDocuments));
            caseData.put(INTERIM_HEARING_UPLOADED_DOCUMENT, additionalUploadedDocuments);
        }

        caseData.put(INTERIM_HEARING_DOCUMENT, interimDocument);
        return documents;
    }

    private CaseDocument prepareInterimHearingRequiredNoticeDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put("ccdCaseNumber", caseDetails.getId());
        caseData.put("courtDetails", buildInterimFrcCourtDetails(caseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        addInterimHearingVenueDetails(caseDetailsCopy);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationInterimHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationInterimHearingNoticeFileName());
    }

    private void addInterimHearingVenueDetails(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            log.info("Interim hearing courtDetailsMap :{}", courtDetailsMap);
            String selectedCourtIH = getSelectedCourtIH(caseData);
            log.info("Interim hearing selectedCourtIH :{}", selectedCourtIH);
            String courtDetailsObj = (String) caseData.get(selectedCourtIH);
            log.info("Interim hearing courtDetailsObj :{}", courtDetailsObj);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(courtDetailsObj);
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public void submitGeneralApplicationDirections(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> documents = prepareDocumentsToPrint(caseDetails, authorisationToken);
        printDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, documents);
        resetStateToGeneralApplicationPrestate(caseDetails);
    }

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

    private List<BulkPrintDocument> prepareDocumentsToPrint(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        List<BulkPrintDocument> documents = new ArrayList<>();
        CaseDocument directionsDocument = caseData.get(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED).equals(YES_VALUE)
            ? prepareHearingRequiredNoticeDocument(caseDetails, authorisationToken)
            : prepareGeneralApplicationDirectionsOrderDocument(caseDetails, authorisationToken);
        documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(directionsDocument));
        caseData.put(GENERAL_APPLICATION_DIRECTIONS_DOCUMENT, directionsDocument);

        Stream.of(GENERAL_APPLICATION_DOCUMENT_LATEST, GENERAL_APPLICATION_DRAFT_ORDER).forEach(documentFieldName -> {
            if (caseData.get(documentFieldName) != null) {
                documents.add(documentHelper.getCaseDocumentAsBulkPrintDocument(
                    documentHelper.convertToCaseDocument(caseData.get(documentFieldName))));
            }
        });

        return documents;
    }

    private void printDocumentPackAndSendToApplicantAndRespondent(CaseDetails caseDetails, String authorisationToken,
                                                                  List<BulkPrintDocument> documents) {
        bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
        bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
    }

    private void resetStateToGeneralApplicationPrestate(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        String generalApplicationPreState = (String) caseData.get(GENERAL_APPLICATION_PRE_STATE);
        if (generalApplicationPreState != null) {
            caseData.put(STATE, generalApplicationPreState);
        }
    }

    private CaseDocument prepareGeneralApplicationDirectionsOrderDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put("courtDetails", buildFrcCourtDetails(caseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        caseData.put("letterDate", String.valueOf(LocalDate.now()));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationOrderTemplate(),
            documentConfiguration.getGeneralApplicationOrderFileName());
    }

    private CaseDocument prepareHearingRequiredNoticeDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put("ccdCaseNumber", caseDetails.getId());
        caseData.put("courtDetails", buildFrcCourtDetails(caseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        addHearingVenueDetails(caseDetailsCopy);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationHearingNoticeFileName());
    }

    private CaseDocument prepareHearingRequiredNoticeDocumentComplexType(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        addDetails(caseData, caseDetails, caseDetailsCopy);
        addHearingVenueDetailsFromDirectionDetailsCollection(caseDetailsCopy);
        caseData.put("letterDate", String.valueOf(LocalDate.now()));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationHearingNoticeTemplate(),
            documentConfiguration.getGeneralApplicationHearingNoticeFileName());
    }

    private void addDetails(Map<String, Object> caseData, CaseDetails caseDetails, CaseDetails caseDetailsCopy) {
        caseData.put("ccdCaseNumber", caseDetails.getId());
        caseData.put("courtDetails", buildFrcCourtDetails(caseData));
        caseData.put("applicantName", documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put("respondentName", documentHelper.getRespondentFullNameContested(caseDetailsCopy));
    }

    private void addHearingVenueDetails(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(caseData.get(getSelectedCourtGA(caseData)));
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void addHearingVenueDetailsFromDirectionDetailsCollection(CaseDetails caseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        Optional<DirectionDetailsCollection> latestDirectionDetails = getLatestDirectionDetails(caseData);
        if (latestDirectionDetails.isPresent()) {
            Map<String, Object> courtDetails = getCourtDetailsFromLatestDirectionDetailsItem(latestDirectionDetails.get());
            caseData.put("hearingVenue", getFrcCourtDetailsAsOneLineAddressString(courtDetails));
            caseData.put("courtDetails", courtDetails);
            caseData.put(GENERAL_APPLICATION_DIRECTIONS_HEARING_DATE, latestDirectionDetails.get().getDateOfHearing());
            caseData.put(GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME, latestDirectionDetails.get().getHearingTime());
            caseData.put(GENERAL_APPLICATION_DIRECTIONS_HEARING_TIME_ESTIMATE, latestDirectionDetails.get().getTimeEstimate());
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

    private boolean isPaperApplication(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(Objects.toString(caseData.get(PAPER_APPLICATION)));
    }

    private boolean isRespondentRepresentedByASolicitor(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONSENTED_RESPONDENT_REPRESENTED)))
            || YES_VALUE.equalsIgnoreCase(nullToEmpty(caseData.get(CONTESTED_RESPONDENT_REPRESENTED)));
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

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }

    private Optional<DirectionDetailsCollection> getLatestDirectionDetails(Map<String, Object> caseData) {
        List<DirectionDetailsCollectionData> directionDetailsCollectionList =
            documentHelper.convertToDirectionDetailsCollectionData(caseData.get(DIRECTION_DETAILS_COLLECTION_CT));

        return Optional.ofNullable(directionDetailsCollectionList).isEmpty()
            ? Optional.empty()
            : Optional.of(directionDetailsCollectionList.get(directionDetailsCollectionList.size() - 1)
            .getDirectionDetailsCollection());
    }
}
