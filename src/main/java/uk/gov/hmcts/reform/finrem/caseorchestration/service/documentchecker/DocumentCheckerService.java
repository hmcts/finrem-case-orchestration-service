package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentchecker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DocumentCheckerService {

    private final EvidenceManagementDownloadService downloadService;

    private final List<DocumentChecker> documentCheckers;

    public DocumentCheckerService(EvidenceManagementDownloadService downloadService,
                                  List<DocumentChecker> documentCheckers) {
        this.downloadService = downloadService;
        this.documentCheckers = documentCheckers;
    }

    public List<String> getWarnings(CaseDocument caseDocument, FinremCaseDetails caseDetails, String authToken) {
        List<DocumentChecker> documentCheckersForDocument = documentCheckers.stream()
            .filter(dc -> dc.canCheck(caseDocument))
            .toList();

        if (documentCheckersForDocument.isEmpty()) {
            return Collections.emptyList();
        }

        byte[] bytes = downloadService.download(caseDocument.getDocumentBinaryUrl(), authToken);

        List<String> warnings = new ArrayList<>();
        documentCheckersForDocument.forEach(dc -> {
            try {
                warnings.addAll(dc.getWarnings(caseDocument, bytes, caseDetails));
            } catch (DocumentContentCheckerException e) {
                log.error("Unexpected error", e);
            }
        });

        return warnings;
    }
}
