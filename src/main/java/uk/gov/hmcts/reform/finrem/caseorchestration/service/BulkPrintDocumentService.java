package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BulkPrintDocumentService {

    private final EvidenceManagementDownloadService service;

    public List<byte[]> downloadDocuments(BulkPrintRequest bulkPrintRequest, String auth) {
        String caseId = bulkPrintRequest.getCaseId();
        log.info("Downloading document for bulk print for case id {}", caseId);

        List<byte[]> documents = bulkPrintRequest.getBulkPrintDocuments().stream()
            .map(bulkPrintDocument -> service.download(bulkPrintDocument.getBinaryFileUrl(), auth))
            .collect(Collectors.toList());
        log.info("Download document count for bulk print {} for case id {} ", documents.size(),
            caseId);
        return documents;
    }
}
