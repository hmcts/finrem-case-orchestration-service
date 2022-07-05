package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.LetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ManualPaymentDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final LetterDetailsMapper letterDetailsMapper;

    public Document generateManualPaymentLetter(FinremCaseDetails caseDetails, String authToken,
                                                DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Applicant Manual Payment Letter {} from {} for bulk print for {}",
            documentConfiguration.getManualPaymentFileName(),
            documentConfiguration.getManualPaymentTemplate(),
            recipient);

        Map<String, Object> placeholdersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails, recipient,
            caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        Document manualPaymentLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(authToken, placeholdersMap,
            documentConfiguration.getManualPaymentTemplate(), documentConfiguration.getManualPaymentFileName());

        log.info("Generated Manual Payment Letter to {}: {}", recipient, manualPaymentLetter);

        return manualPaymentLetter;
    }
}
