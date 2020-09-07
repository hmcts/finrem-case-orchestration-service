package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.buildCourtDetails;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getCourtDetailsString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseHearingFunctions.getSelectedCourt;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManualPaymentDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final ObjectMapper objectMapper;

    private static final String APPLICANT = "Applicant";
    private static final String RESPONDENT = "Respondent";

    private CaseDetails caseDetailsForBulkPrint;

    public CaseDocument generateManualPaymentLetter(CaseDetails caseDetails, String authToken, String party) {
        log.info("Generating Manual Payment Letter {} from {} for bulk print",
            documentConfiguration.getManualPaymentFileName(),
            documentConfiguration.getManualPaymentTemplate());

        if (party.equals(APPLICANT)) {
            caseDetailsForBulkPrint = documentHelper.prepareLetterToPartyTemplateData(caseDetails, APPLICANT);
        } else {
            caseDetailsForBulkPrint = documentHelper.prepareLetterToPartyTemplateData(caseDetails, RESPONDENT);
        }

        addCourtFields(caseDetailsForBulkPrint);

        CaseDocument generatedManualPaymentLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getManualPaymentTemplate(),
            documentConfiguration.getManualPaymentFileName());
        log.info("Generated Manual Payment Letter: {}", generatedManualPaymentLetter);

        return generatedManualPaymentLetter;
    }

    private CaseDetails addCourtFields(CaseDetails caseDetails) {
        try {
            Map<String, Object> courtDetailsMap = objectMapper.readValue(getCourtDetailsString(), HashMap.class);
            Map<String, Object> data = caseDetails.getData();
            Map<String, Object> courtDetails = (Map<String, Object>) courtDetailsMap.get(data.get(getSelectedCourt(data)));
            data.put("courtDetails", buildCourtDetails(courtDetails));
            return caseDetails;
        } catch (IOException | NullPointerException e) {
            return caseDetails;
        }
    }
}
