package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintService;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BulkPrintServiceAdapter {

    private final BulkPrintService bulkPrintService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    /**
     * Adapter for legacy {@link CaseDetails} that prints the applicant's documents for a case
     * by delegating to {@link BulkPrintService#printApplicantDocuments}.
     *
     * @param caseDetails the case details containing the applicant's data
     * @param authToken the authorization token used to access downstream services
     * @param documents the documents to send for printing
     * @return the UUID of the created print batch
     */
    public UUID printApplicantDocuments(CaseDetails caseDetails, String authToken, List<BulkPrintDocument> documents) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        return bulkPrintService.printApplicantDocuments(finremCaseDetails, authToken, documents);
    }

    /**
     * Adapter for legacy {@link CaseDetails} that prints the respondent's documents for a case
     * by delegating to {@link BulkPrintService#printApplicantDocuments}.
     *
     * @param caseDetails the case details containing the applicant's data
     * @param authToken the authorization token used to access downstream services
     * @param documents the documents to send for printing
     * @return the UUID of the created print batch
     */
    public UUID printRespondentDocuments(CaseDetails caseDetails, String authToken, List<BulkPrintDocument> documents) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        return bulkPrintService.printRespondentDocuments(finremCaseDetails, authToken, documents);
    }

    /**
     * Adapter for legacy {@link CaseDetails} that prints the documents for a given recipient
     * by delegating to {@link BulkPrintService#printApplicantDocuments}.
     *
     * @param document the documents to send for printing
     * @param caseDetails the case details containing the applicant's data
     * @param recipient the recipient of the printed documents
     * @param auth the authorization token used to access downstream services
     * @return the UUID of the created print batch
     */
    public UUID sendDocumentForPrint(final CaseDocument document, CaseDetails caseDetails,
                                     final String recipient, String auth) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        return bulkPrintService.sendDocumentForPrint(document, finremCaseDetails, recipient, auth);
    }

    /**
     * Formats recipient by capitalizing each word separated by underscores
     * and then removes the underscores from the resulting string.
     *
     * @param text the input string where words are separated by underscores
     * @return a formatted string with words capitalized and underscores removed
     */
    public String getRecipient(String text) {
        return StringUtils.remove(WordUtils.capitalizeFully(text, '_'), '_');
    }

}
