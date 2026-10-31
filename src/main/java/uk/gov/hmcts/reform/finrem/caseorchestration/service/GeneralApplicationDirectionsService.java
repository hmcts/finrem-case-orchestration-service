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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralApplicationWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.IntervenerOne;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.intevener.IntervenerWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER1;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER2;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER3;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.INTERVENER4;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LIST_FOR_INTERIM_HEARING;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PREPARE_FOR_HEARING_STATE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildFrcCourtDetails;

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

    private static final String COURT_DETAIL = "courtDetails";
    private static final String APPLICANT_NAME = "applicantName";
    private static final String RESPONDENT_NAME = "respondentName";
    private static final String LETTER_DATE = "letterDate";

    public String getEventPostState(FinremCaseDetails finremCaseDetails, String userAuthorisation) {
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        List<String> eventDetailsOnCase = ccdService.getCcdEventDetailsOnCase(
                userAuthorisation,
                caseDetails,
                EventType.GENERAL_APPLICATION_DIRECTIONS_MH.getCcdType())
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

    /**
     * Sends the supplied general application direction documents via bulk print to the relevant parties.
     *
     * <p>Printing only happens when the general application "refer detail" is present (non-empty).
     * When it is, the documents are always sent to the applicant and the respondent, and are additionally
     * sent to an intervener if the refer detail identifies one (see {@link #sendIntervenerDocuments}).
     * When the refer detail is null or empty, no documents are sent.</p>
     *
     * @param caseDetails         the case details containing the general application wrapper and party data
     * @param documents           the documents to be sent via bulk print
     * @param authorisationToken  the user's authorisation token used for the bulk print requests
     */
    public void submitCollectionGeneralApplicationDirections(FinremCaseDetails caseDetails, List<BulkPrintDocument> documents,
                                                             String authorisationToken) {
        String referDetail = caseDetails.getData().getGeneralApplicationWrapper().getGeneralApplicationReferDetail();
        log.info("The relevant party {} for Case ID: {}", ObjectUtils.nullSafeConciseToString(referDetail), caseDetails.getId());
        if (StringUtils.isNotEmpty(referDetail)) {
            log.info("Sending {} document(s) to applicant via bulk print for Case ID: {}, document(s) are {}",
                documents.size(), caseDetails.getId(),
                documents);

            bulkPrintService.printRespondentDocuments(caseDetails, authorisationToken, documents);
            log.info("Sending {} document(s) to respondent via bulk print for Case ID: {}, document(s) are {}",
                documents.size(), caseDetails.getId(),
                documents);
            bulkPrintService.printApplicantDocuments(caseDetails, authorisationToken, documents);

            sendIntervenerDocuments(caseDetails, authorisationToken, documents, referDetail);
        }
    }

    /**
     * Generates a General Application Directions Order document when a hearing is not required.
     * If a hearing is required, returns an empty {@link Optional}.
     *
     * @param authorisationToken the authorisation token used for document generation
     * @param finremCaseDetails the details of the financial remedy case for which the document is generated
     * @return an {@link Optional} containing the generated {@link CaseDocument} if created; otherwise {@link Optional#empty()}.
     */
    public Optional<CaseDocument> generateGeneralApplicationDirectionsDocumentIfNeeded(
        String authorisationToken, FinremCaseDetails finremCaseDetails) {

        if (isHearingRequired(finremCaseDetails)) {
            return Optional.empty();
        }

        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        CaseDocument generalApplicationDirectionsOrderDocument =
            prepareGeneralApplicationDirectionsOrderDocument(caseDetails, authorisationToken);

        return Optional.of(generalApplicationDirectionsOrderDocument);
    }

    public boolean isHearingRequired(FinremCaseDetails caseDetails) {
        YesOrNo hearingRequired = caseDetails.getData()
            .getGeneralApplicationWrapper()
            .getGeneralApplicationDirectionsHearingRequired();

        return YesOrNo.isYes(hearingRequired);
    }

    private void sendIntervenerDocuments(FinremCaseDetails caseDetails, String authorisationToken,
                                         List<BulkPrintDocument> documents, String referDetail) {
        if (referDetail.contains(INTERVENER1.toLowerCase()) || referDetail.contains(INTERVENER1)) {
            IntervenerOne intervenerWrapper = caseDetails.getData().getIntervenerOne();
            sendToBulkPrintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        } else if (referDetail.contains(INTERVENER2.toLowerCase()) || referDetail.contains(INTERVENER2)) {
            IntervenerWrapper intervenerWrapper = caseDetails.getData().getIntervenerTwo();
            sendToBulkPrintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        } else if (referDetail.contains(INTERVENER3.toLowerCase()) || referDetail.contains(INTERVENER3)) {
            IntervenerWrapper intervenerWrapper = caseDetails.getData().getIntervenerThree();
            sendToBulkPrintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        } else if (referDetail.contains(INTERVENER4.toLowerCase()) || referDetail.contains(INTERVENER4)) {
            IntervenerWrapper intervenerWrapper = caseDetails.getData().getIntervenerFour();
            sendToBulkPrintForIntervener(caseDetails, authorisationToken, documents, intervenerWrapper);
        }
    }

    private void sendToBulkPrintForIntervener(FinremCaseDetails caseDetails, String authorisationToken,
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
}
