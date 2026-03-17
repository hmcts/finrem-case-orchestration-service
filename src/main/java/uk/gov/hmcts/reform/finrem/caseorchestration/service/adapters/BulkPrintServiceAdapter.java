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

    public UUID printApplicantDocuments(CaseDetails caseDetails, String authToken, List<BulkPrintDocument> documents) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        return bulkPrintService.printApplicantDocuments(finremCaseDetails, authToken, documents);
    }

    public UUID printRespondentDocuments(CaseDetails caseDetails, String authToken, List<BulkPrintDocument> documents) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        return bulkPrintService.printRespondentDocuments(finremCaseDetails, authToken, documents);
    }

    public UUID sendDocumentForPrint(final CaseDocument document, CaseDetails caseDetails,
                                     final String recipient, String auth) {
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        return bulkPrintService.sendDocumentForPrint(document, finremCaseDetails, recipient, auth);
    }

    public String getRecipient(String text) {
        return StringUtils.remove(WordUtils.capitalizeFully(text, '_'), '_');
    }

}
