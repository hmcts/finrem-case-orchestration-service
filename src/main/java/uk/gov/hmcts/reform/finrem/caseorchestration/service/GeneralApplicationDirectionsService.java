package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PREPARE_FOR_HEARING_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService.nullToEmpty;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getFrcCourtDetailsAsOneLineAddressString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourtGA;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationDirectionsService {

    private final BulkPrintService bulkPrintService;
    private final DocumentConfiguration documentConfiguration;
    private final GenericDocumentService genericDocumentService;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;
    private final CcdService ccdService;

    private static final String CASE_NUMBER = "ccdCaseNumber";
    private static final String COURT_DETAIL = "courtDetails";
    private static final String APPLICANT_NAME = "applicantName";
    private static final String RESPONDENT_NAME = "respondentName";
    private static final String LETTER_DATE = "letterDate";

    public String getEventPostState(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        List<String> eventDetailsOnCase = ccdService.getCcdEventDetailsOnCase(
                userAuthorisation,
                caseDetails,
                EventType.GENERAL_APPLICATION_DIRECTIONS.getCcdType())
            .stream()
            .map(CaseEventDetail::getEventName).toList();

        log.info("Previous event names : {} for Case ID: {}", eventDetailsOnCase, caseDetails.getId());
        String hearingOption = Objects.toString(finremCaseDetails.getData().getGeneralApplicationWrapper()
            .getGeneralApplicationDirectionsHearingRequired(), null);
        log.info("Hearing option selected on direction : {} for Case ID: {}", hearingOption, caseDetails.getId());

        if ((!eventDetailsOnCase.isEmpty() && (eventDetailsOnCase.contains(LIST_FOR_HEARING)
            || eventDetailsOnCase.contains(LIST_FOR_INTERIM_HEARING)))
            || (hearingOption != null && hearingOption.equals(YES_VALUE))) {
            return PREPARE_FOR_HEARING_STATE;
        }

        String previousState = Objects.toString(finremCaseDetails.getData().getGeneralApplicationWrapper()
            .getGeneralApplicationPreState(), null);
        log.info("Previous state : {} for Case ID: {}", previousState, caseDetails.getId());
        return previousState;
    }

    public void resetGeneralApplicationDirectionsFields(FinremCaseData caseData) {

        GeneralApplicationWrapper generalApplicationWrapper = caseData.getGeneralApplicationWrapper();
        generalApplicationWrapper.setGeneralApplicationDirectionsHearingRequired(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsHearingDate(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsHearingTime(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsHearingTimeEstimate(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsAdditionalInformation(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsCourtOrderDate(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsJudgeType(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsJudgeName(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsRecitals(null);
        generalApplicationWrapper.setGeneralApplicationDirectionsTextFromJudge(null);
        caseData.getRegionWrapper().getGeneralApplicationRegionWrapper().clearRegions();
    }

    public void submitCollectionGeneralApplicationDirections(FinremCaseDetails caseDetails, List<BulkPrintDocument> dirDocuments,
                                                             String authorisationToken) {
        printDocumentPackAndSendToRelevantParties(caseDetails, authorisationToken, dirDocuments);
    }

    public CaseDocument getBulkPrintDocument(CaseDetails caseDetails, String authorisationToken) {
        Map<String, Object> caseData = caseDetails.getData();
        return caseData.get(GENERAL_APPLICATION_DIRECTIONS_HEARING_REQUIRED).equals(YES_VALUE)
            ? prepareHearingRequiredNoticeDocument(caseDetails, authorisationToken)
            : prepareGeneralApplicationDirectionsOrderDocument(caseDetails, authorisationToken);
    }

    private void printDocumentPackAndSendToRelevantParties(FinremCaseDetails caseDetails, String authorisationToken,
                                                           List<BulkPrintDocument> documents) {
        String referDetail = caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferDetail();
        log.info("The relevant party {} for Case ID: {}", ObjectUtils.nullSafeConciseToString(referDetail), caseDetails.getId());
        if (StringUtils.isNotEmpty(referDetail)) {
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);
            log.info("Sending {} document(s) to applicant via bulk print for Case ID: {}, document(s) are {}",
                documents.size(), caseDetails.getId(),
                documents);
            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
            log.info("Sending {} document(s) to respondent via bulk print for Case ID: {}, document(s) are {}",
                documents.size(), caseDetails.getId(),
                documents);
            sendIntervenerDocuments(caseDetails, authorisationToken, documents, referDetail);
        }
    }

    private void sendIntervenerDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> documents, String referDetail) {
        if (referDetail.contains(INTERVENER1.toLowerCase()) || referDetail.contains(INTERVENER1)) {
            IntervenerOne intervenerWrapper = caseDetails.getData().getIntervenerOne();
            sendToBulkprintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        } else if (referDetail.contains(INTERVENER2.toLowerCase()) || referDetail.contains(INTERVENER2)) {
            IntervenerWrapper intervenerWrapper = caseDetails.getData().getIntervenerTwo();
            sendToBulkprintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        } else if (referDetail.contains(INTERVENER3.toLowerCase()) || referDetail.contains(INTERVENER3)) {
            IntervenerWrapper intervenerWrapper = caseDetails.getData().getIntervenerThree();
            sendToBulkprintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        } else if (referDetail.contains(INTERVENER4.toLowerCase()) || referDetail.contains(INTERVENER4)) {
            IntervenerWrapper intervenerWrapper = caseDetails.getData().getIntervenerFour();
            sendToBulkprintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        }
    }

    private void sendToBulkprintForIntervener(FinremCaseDetails caseDetails, String authorisationToken,
                                              List<BulkPrintDocument> documents,
                                              IntervenerWrapper intervenerWrapper) {
        bulkPrintService.printIntervenerDocuments(intervenerWrapper, caseDetails, authorisationToken, documents);
        log.info("Sending {} document(s) to {} via bulk print for Case ID: {}, document(s) are {}",
            intervenerWrapper.getIntervenerType(), documents.size(), caseDetails.getId(),
            documents);
    }

    @SuppressWarnings("java:S1874")
    private CaseDocument prepareGeneralApplicationDirectionsOrderDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put(COURT_DETAIL, buildFrcCourtDetails(caseData));
        caseData.put(APPLICANT_NAME, documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put(RESPONDENT_NAME, documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        caseData.put(LETTER_DATE, String.valueOf(LocalDate.now()));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationOrderTemplate(caseDetails),
            documentConfiguration.getGeneralApplicationOrderFileName());
    }

    @SuppressWarnings("java:S1874")
    private CaseDocument prepareHearingRequiredNoticeDocument(CaseDetails caseDetails, String authorisationToken) {
        CaseDetails caseDetailsCopy = documentHelper.deepCopy(caseDetails, CaseDetails.class);
        Map<String, Object> caseData = caseDetailsCopy.getData();

        caseData.put(CASE_NUMBER, caseDetails.getId());
        caseData.put(COURT_DETAIL, buildFrcCourtDetails(caseData));
        caseData.put(APPLICANT_NAME, documentHelper.getApplicantFullName(caseDetailsCopy));
        caseData.put(RESPONDENT_NAME, documentHelper.getRespondentFullNameContested(caseDetailsCopy));
        addHearingVenueDetails(caseDetailsCopy);
        caseData.put(LETTER_DATE, String.valueOf(LocalDate.now()));

        return genericDocumentService.generateDocument(authorisationToken, caseDetailsCopy,
            documentConfiguration.getGeneralApplicationHearingNoticeTemplate(caseDetails),
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

    public boolean isNotEmpty(String field, Map<String, Object> caseData) {
        return StringUtils.isNotEmpty(nullToEmpty(caseData.get(field)));
    }
}
