package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.BulkPrintRequest;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BulkPrintDocumentServiceTest {

    private static final String FILE_URL = "http://dm:80/documents/kbjh87y8y9JHVKKKJVJ";
    private final byte[] someBytes = "ainhsdcnoih".getBytes();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks private BulkPrintDocumentService service;

    @Mock private EvidenceManagementDownloadService evidenceManagementService;

    @Test
    public void downloadDocuments() {
        when(evidenceManagementService.download(FILE_URL)).thenReturn(ResponseEntity.ok(someBytes));

        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder().binaryFileUrl(FILE_URL).build()))
            .build();

        List<byte[]> result = service.downloadDocuments(bulkPrintRequest);
        assertThat(result.get(0), is(equalTo(someBytes)));
    }

    @Test
    public void throwsExceptionOnBadRequest() {
        when(evidenceManagementService.download(FILE_URL)).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(someBytes));

        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder().binaryFileUrl(FILE_URL).build()))
            .build();

        thrown.expect(RuntimeException.class);
        service.downloadDocuments(bulkPrintRequest);
    }

    @Test
    public void throwsExceptionOnAnyException() {
        when(evidenceManagementService.download(FILE_URL)).thenThrow(new RuntimeException());

        BulkPrintRequest bulkPrintRequest = BulkPrintRequest.builder()
            .bulkPrintDocuments(singletonList(BulkPrintDocument.builder().binaryFileUrl(FILE_URL).build()))
            .build();

        thrown.expect(RuntimeException.class);
        service.downloadDocuments(bulkPrintRequest);
    }
}
