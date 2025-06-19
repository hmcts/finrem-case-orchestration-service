package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

@Service
@Slf4j
@RequiredArgsConstructor
public class HelpWithFeesDocumentService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final DocumentHelper documentHelper;

    /**
     * Generates the "Help With Fees Successful" notification letter for a given case and recipient.
     *
     * <p>This method prepares the template data based on the case details and the intended paper
     * notification recipient, then uses the {@code genericDocumentService} to generate the document
     * using the configured template and file name.</p>
     *
     * <p>The generated document is typically used for bulk print operations.</p>
     *
     * @param caseDetails the case details used to populate the template
     * @param authToken the authorisation token for generating the document
     * @param recipient the intended paper notification recipient (e.g. applicant or respondent)
     * @return the generated notification letter as a {@link CaseDocument}
     */
    public CaseDocument generateHwfSuccessfulNotificationLetter(FinremCaseDetails caseDetails, String authToken,
                                                                DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Help With Fees Successful Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            recipient);

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);

        CaseDocument generatedHwfSuccessfulNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName());

        log.info("Generated Help With Fees Successful Notification Letter: {}", generatedHwfSuccessfulNotificationLetter);

        return generatedHwfSuccessfulNotificationLetter;
    }

    @Deprecated
    public CaseDocument generateHwfSuccessfulNotificationLetter(CaseDetails caseDetails, String authToken,
                                                                DocumentHelper.PaperNotificationRecipient recipient) {
        log.info("Generating Help With Fees Successful Notification Letter {} from {} for bulk print for {}",
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            recipient);

        CaseDetails caseDetailsForBulkPrint = documentHelper.prepareLetterTemplateData(caseDetails, recipient);

        CaseDocument generatedHwfSuccessfulNotificationLetter = genericDocumentService.generateDocument(authToken,
            caseDetailsForBulkPrint,
            documentConfiguration.getHelpWithFeesSuccessfulNotificationTemplate(),
            documentConfiguration.getHelpWithFeesSuccessfulNotificationFileName());

        log.info("Generated Help With Fees Successful Notification Letter: {}", generatedHwfSuccessfulNotificationLetter);

        return generatedHwfSuccessfulNotificationLetter;
    }
}
