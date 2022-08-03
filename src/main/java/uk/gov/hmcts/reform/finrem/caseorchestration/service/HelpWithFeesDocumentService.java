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
public class HelpWithFeesDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;
    private final LetterDetailsMapper letterDetailsMapper;

    public Document generateHwfSuccessfulNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Help With Fees Successful Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            recipient);

        Map<String, Object> placeHoldersMap = letterDetailsMapper.getLetterDetailsAsMap(caseDetails,
            recipient, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        Document generatedHwfSuccessfulNotificationLetter = genericDocumentService.generateDocumentFromPlaceholdersMap(authToken,
            placeHoldersMap,
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName());

        log.info("Generated Help With Fees Successful Notification Letter: {}", generatedHwfSuccessfulNotificationLetter);

        return generatedHwfSuccessfulNotificationLetter;
    }
}
