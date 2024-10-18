package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AdditionalHearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenerateCoverSheetService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ValidateHearingService;

import java.util.List;

@Slf4j
@Service
public class ListForHearingContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final HearingDocumentService hearingDocumentService;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final ValidateHearingService validateHearingService;
    private final CaseDataService caseDataService;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final GenerateCoverSheetService coverSheetService;

    public ListForHearingContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, HearingDocumentService hearingDocumentService,
                                                       AdditionalHearingDocumentService additionalHearingDocumentService,
                                                       CaseDataService caseDataService,
                                                       ValidateHearingService validateHearingService, ObjectMapper objectMapper,
                                                       NotificationService notificationService,
                                                       GenerateCoverSheetService coverSheetService) {
        super(finremCaseDetailsMapper);
        this.hearingDocumentService = hearingDocumentService;
        this.additionalHearingDocumentService = additionalHearingDocumentService;
        this.validateHearingService = validateHearingService;
        this.caseDataService = caseDataService;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
        this.coverSheetService = coverSheetService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.LIST_FOR_HEARING.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        validateCaseData(callbackRequest);
        FinremCaseDetails finremCaseDetails = callbackRequest.getCaseDetails();
        FinremCaseData finremCaseData = finremCaseDetails.getData();
        String caseId = finremCaseDetails.getId().toString();
        log.info("Invoking contested {} about to submit callback for Case ID: {}",
            callbackRequest.getEventType(), caseId);

        List<String> errors = validateHearingService.validateHearingErrors(finremCaseDetails);

        if (!errors.isEmpty()) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().errors(errors).build();
        }

        if (finremCaseData.getAdditionalListOfHearingDocuments() != null) {
            CaseDocument caseDocument = objectMapper.convertValue(finremCaseData.getAdditionalListOfHearingDocuments(),
                CaseDocument.class);
            CaseDocument pdfDocument = additionalHearingDocumentService.convertToPdf(caseDocument, userAuthorisation, caseId);
            finremCaseData.setAdditionalListOfHearingDocuments(pdfDocument);
        }
        CaseDetails caseDetails = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails);
        if (hearingDocumentService.alreadyHadFirstHearing(finremCaseDetails)) {
            if (caseDataService.isContestedApplication(finremCaseDetails)) {
                try {
                    additionalHearingDocumentService.createAdditionalHearingDocuments(userAuthorisation, caseDetails);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            caseDetails.getData().putAll(hearingDocumentService.generateHearingDocuments(userAuthorisation, caseDetails));
        }


        finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        finremCaseData = finremCaseDetails.getData();
        List<String> warnings = validateHearingService.validateHearingWarnings(finremCaseDetails);
        log.info("Hearing date warning {} Case ID: {}", warnings, caseId);

        if (!notificationService.isApplicantSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            CaseDocument coverSheet = coverSheetService.generateApplicantCoverSheet(finremCaseDetails, userAuthorisation);
            log.info("Applicant coversheet generated and attach to case {}  for Case ID: {}", caseId, coverSheet);
            populateApplicantBulkPrintFieldsWithCoverSheet(finremCaseData, caseId, coverSheet);
        }

        if (!notificationService.isRespondentSolicitorDigitalAndEmailPopulated(finremCaseDetails)) {
            CaseDocument coverSheet = coverSheetService.generateRespondentCoverSheet(finremCaseDetails, userAuthorisation);
            log.info("Respondent coversheet generated and attach to case {}  for Case ID: {}", caseId, coverSheet);
            populateRespondentBulkPrintFieldsWithCoverSheet(finremCaseData, coverSheet, caseId);
        }
        callbackRequest.getCaseDetails().setData(finremCaseData);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).warnings(warnings).build();
    }

    private void populateApplicantBulkPrintFieldsWithCoverSheet(FinremCaseData finremCaseData, String caseId, CaseDocument coverSheet) {
        if (caseDataService.isApplicantAddressConfidential(finremCaseData)) {
            log.info("Applicant has been marked as confidential, adding coversheet to confidential field for Case ID: {}", caseId);
            finremCaseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(null);
            finremCaseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetAppConfidential(coverSheet);
        } else {
            log.info("Applicant adding coversheet to coversheet field for Case ID: {}", caseId);
            finremCaseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetApp(coverSheet);
        }
    }

    private void populateRespondentBulkPrintFieldsWithCoverSheet(FinremCaseData finremCaseData, CaseDocument coverSheet, String caseId) {
        if (caseDataService.isRespondentAddressConfidential(finremCaseData)) {
            log.info("Respondent has been marked as confidential, adding coversheet to confidential field for Case ID: {}", caseId);
            finremCaseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(null);
            finremCaseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetResConfidential(coverSheet);
        } else {
            log.info("Respondent adding coversheet to coversheet field for Case ID: {}", caseId);
            finremCaseData.getBulkPrintCoversheetWrapper().setBulkPrintCoverSheetRes(coverSheet);
        }
    }

}
