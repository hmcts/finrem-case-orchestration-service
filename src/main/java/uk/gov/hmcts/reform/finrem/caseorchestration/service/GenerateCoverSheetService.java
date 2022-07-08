package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.DocumentConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint.BulkPrintCoverLetterDetailsMapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.Document;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseDetails;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateCoverSheetService {

    private final GenericDocumentService genericDocumentService;
    private final DocumentConfiguration documentConfiguration;
    private final BulkPrintCoverLetterDetailsMapper bulkPrintCoverLetterDetailsMapper;

    public Document generateApplicantCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Applicant cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.APPLICANT);
    }

    public Document generateRespondentCoverSheet(final FinremCaseDetails caseDetails, final String authorisationToken) {
        log.info("Generating Respondent cover sheet {} from {} for bulk print", documentConfiguration.getBulkPrintFileName(),
            documentConfiguration.getBulkPrintTemplate());

        return generateCoverSheet(caseDetails, authorisationToken, DocumentHelper.PaperNotificationRecipient.RESPONDENT);
    }

    private Document generateCoverSheet(FinremCaseDetails caseDetails,
                                        String authorisationToken,
                                        DocumentHelper.PaperNotificationRecipient recipient) {

        Map<String, Object> placeholdersMap = bulkPrintCoverLetterDetailsMapper
            .getLetterDetailsAsMap(caseDetails, recipient, caseDetails.getCaseData().getRegionWrapper().getDefaultCourtList());

        return genericDocumentService.generateDocumentFromPlaceholdersMap(authorisationToken, placeholdersMap,
        documentConfiguration.getBulkPrintTemplate(), documentConfiguration.getBulkPrintFileName());
    }
}
