package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BulkPrintDocumentService {

    private final EvidenceManagementDownloadService service;

    public List<byte[]> downloadDocuments(BulkPrintRequest bulkPrintRequest) {
        log.info("Downloading document for bulk print for case id {}", bulkPrintRequest.getCaseId());

        List<byte[]> documents = bulkPrintRequest.getBulkPrintDocuments().stream().map(bulkPrintDocument -> {
            ResponseEntity<byte[]> response = service.download(bulkPrintDocument.getBinaryFileUrl());
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Download failed for url {} ", bulkPrintDocument.getBinaryFileUrl());
                throw new RuntimeException(String.format("Unexpected error DM store: %s ", response.getStatusCode()));
            }
            return response.getBody();
        }).collect(Collectors.toList());
        log.info("Download document count for bulk print {} for case id {} ", documents.size(),
            bulkPrintRequest.getCaseId());
        return documents;
    }
}
