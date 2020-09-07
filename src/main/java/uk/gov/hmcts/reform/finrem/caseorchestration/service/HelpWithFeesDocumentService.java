package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Service
@Slf4j
@RequiredArgsConstructor
public class HelpWithFeesDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;

    public CaseDocument generateHwfSuccessfulNotificationLetter(CaseDetails caseDetails, String authToken) {
        log.info("Generating Help With Fees Successful Notification Letter {} from {} for bulk print",
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate());

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterToPartyTemplateData(caseDetails, "Applicant");

        CaseDocument generatedHwfSuccessfulNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName());

        log.info("Generated Help With Fees Successful Notification Letter: {}", generatedHwfSuccessfulNotificationLetter);

        return generatedHwfSuccessfulNotificationLetter;
    }
}
