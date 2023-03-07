package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.StampDocumentException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.COURT_SEAL_IMAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.fileUploadResponse;

@RunWith(MockitoJUnitRunner.class)
public class PdfStampingServiceTest {

    public static final String COURT_SEAL_PDF = "/courtseal.pdf";

    @InjectMocks private PdfStampingService service;

    @Mock private EvidenceManagementUploadService evidenceManagementUploadServiceService;
    @Mock private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @Test(expected = StampDocumentException.class)
    public void shouldThrowExceptionWhenDocumentIsNotPdf() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_IMAGE);
        when(evidenceManagementDownloadService.download(document.getBinaryUrl()))
            .thenReturn(ResponseEntity.ok(imageAsBytes));

        service.stampDocument(document, "auth", false);
    }

    @Test
    public void shouldAddAnnexAndStampToDocument() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_PDF);

        when(evidenceManagementDownloadService.download(document.getBinaryUrl()))
            .thenReturn(ResponseEntity.ok(imageAsBytes));

        when(evidenceManagementUploadServiceService.upload(any(), anyString()))
            .thenReturn(fileUploadResponse());

        Document stampDocument = service.stampDocument(document, "auth", true);

        assertThat(stampDocument, not(equalTo(imageAsBytes)));
        assertThat(stampDocument.getFileName(), is(document.getFileName()));
        assertThat(stampDocument.getBinaryUrl(), is(document.getBinaryUrl()));
        assertThat(stampDocument.getUrl(), is(document.getUrl()));
    }

    @Test
    public void shouldAddStampToDocument() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_PDF);

        when(evidenceManagementDownloadService.download(document.getBinaryUrl()))
            .thenReturn(ResponseEntity.ok(imageAsBytes));

        when(evidenceManagementUploadServiceService.upload(any(), anyString()))
            .thenReturn(fileUploadResponse());

        Document stampDocument = service.stampDocument(document, "auth", false);

        assertThat(stampDocument, not(equalTo(imageAsBytes)));
        assertThat(stampDocument.getFileName(), is(document.getFileName()));
        assertThat(stampDocument.getBinaryUrl(), is(document.getBinaryUrl()));
        assertThat(stampDocument.getUrl(), is(document.getUrl()));
    }

    @Test
    public void shouldGetImageAsBytes() throws Exception {
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_IMAGE);

        assertThat(imageAsBytes, notNullValue());
    }
}
