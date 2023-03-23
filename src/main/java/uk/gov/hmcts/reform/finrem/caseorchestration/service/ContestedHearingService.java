package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_ADDITIONAL_DOC;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContestedHearingService {

    private final ObjectMapper objectMapper;
    private final AdditionalHearingDocumentService additionalHearingDocumentService;
    private final FinremHearingDocumentService hearingDocumentService;
    private final ValidateHearingService validateHearingService;
    private final CaseDataService caseDataService;


    private List<String> nonFastTrackWarningsList = new ArrayList<>();
    private List<String> fastTrackWarningsList = new ArrayList<>();

    public void prepareForHearing(FinremCallbackRequest callbackRequest, String authorisationToken) throws JsonProcessingException {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseDocument caseDocument = caseDetails.getData().getAdditionalListOfHearingDocuments();
        if (caseDocument != null) {
            CaseDocument pdfDocument = additionalHearingDocumentService.convertToPdf(caseDocument, authorisationToken);
            caseDetails.getData().setAdditionalListOfHearingDocuments(pdfDocument);
        }

        if (hearingDocumentService.alreadyHadFirstHearing(caseDetails)) {
            if (caseDetails.getData().isContestedApplication()) {
                additionalHearingDocumentService.createAdditionalHearingDocuments(authorisationToken, caseDetails);
            }
        } else {
            caseDetails.getData().getAdditionalListOfHearingDocuments(hearingDocumentService.generateHearingDocuments(authorisationToken, caseDetails));
        }

        List<String> warnings = validateHearingService.validateHearingWarnings(caseDetails, fastTrackWarningsList, nonFastTrackWarningsList);
        log.info("Hearing date warning {} Case ID: {}",warnings, caseDetails.getId());
        if ((warnings.isEmpty() || fastTrackWarningsList.size() > 1 || nonFastTrackWarningsList.size() > 1)
            && caseDataService.isContestedApplication(caseDetails)) {
            if (caseDetailsBefore != null && hearingDocumentService.alreadyHadFirstHearing(caseDetailsBefore)) {
                log.info("Sending Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
                additionalHearingDocumentService.sendAdditionalHearingDocuments(authorisationToken, caseDetails);
                log.info("Sent Additional Hearing Document to bulk print for Contested Case ID: {}", caseDetails.getId());
            } else {
                log.info("Sending Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
                hearingDocumentService.sendInitialHearingCorrespondence(caseDetails, authorisationToken);
                log.info("sent Forms A, C, G to bulk print for Contested Case ID: {}", caseDetails.getId());
            }
            fastTrackWarningsList = new ArrayList<>();
            nonFastTrackWarningsList = new ArrayList<>();
        }

    }
}

