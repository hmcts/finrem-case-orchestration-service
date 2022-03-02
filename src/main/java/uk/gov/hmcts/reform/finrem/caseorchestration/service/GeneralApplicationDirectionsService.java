package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtGA;

@Service
@RequiredArgsConstructor
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

    public void submitGeneralApplicationDirections(CaseDetails caseDetails, String authorisationToken) {
        List<BulkPrintDocument> documents = prepareDocumentsToPrint(caseDetails, authorisationToken);
        printDocumentPackAndSendToApplicantAndRespondent(caseDetails, authorisationToken, documents);
        resetStateToGeneralApplicationPrestate(caseDetails);
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
}
