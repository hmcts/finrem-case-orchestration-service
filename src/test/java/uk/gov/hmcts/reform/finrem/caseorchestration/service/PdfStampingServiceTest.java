package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.StampDocumentException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.COURT_SEAL_IMAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.fileUploadResponse;

@ExtendWith(MockitoExtension.class)
class PdfStampingServiceTest {

    public static final String COURT_SEAL_PDF = "/courtseal.pdf";
    public static final String HIGH_COURT_SEAL_PDF = "/highcourtseal.pdf";

    @InjectMocks private PdfStampingService service;

    @Mock private EvidenceManagementUploadService evidenceManagementUploadServiceService;
    @Mock private EvidenceManagementDownloadService evidenceManagementDownloadService;
    @Mock private DocumentConversionService documentConversionService;

    @Test
    void shouldThrowExceptionWhenDocumentIsNotPdf() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_IMAGE);
        when(evidenceManagementDownloadService.download(document.getBinaryUrl(), AUTH_TOKEN))
            .thenReturn(imageAsBytes);

        assertThrows(StampDocumentException.class, () -> {
            service.stampDocument(document, AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, CASE_ID);
        });
    }

    @Test
    void shouldAddAnnexAndStampToDocument() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_PDF);

        when(evidenceManagementDownloadService.download(document.getBinaryUrl(), AUTH_TOKEN))
            .thenReturn(imageAsBytes);

        when(evidenceManagementUploadServiceService.upload(any(), anyString(), any()))
            .thenReturn(fileUploadResponse());

        Document stampDocument = service.stampDocument(document, AUTH_TOKEN, true, StampType.FAMILY_COURT_STAMP, CASE_ID);

        assertThat(stampDocument, not(equalTo(imageAsBytes)));
        assertThat(stampDocument.getFileName(), is(document.getFileName()));
        assertThat(stampDocument.getBinaryUrl(), is(document.getBinaryUrl()));
        assertThat(stampDocument.getUrl(), is(document.getUrl()));
    }

    @Test
    void shouldAddStampToDocument() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_PDF);

        when(evidenceManagementDownloadService.download(document.getBinaryUrl(), AUTH_TOKEN))
            .thenReturn(imageAsBytes);

        when(evidenceManagementUploadServiceService.upload(any(), anyString(), any()))
            .thenReturn(fileUploadResponse());

        Document stampDocument = service.stampDocument(document, AUTH_TOKEN, false, StampType.FAMILY_COURT_STAMP, CASE_ID);

        assertThat(stampDocument, not(equalTo(imageAsBytes)));
        assertThat(stampDocument.getFileName(), is(document.getFileName()));
        assertThat(stampDocument.getBinaryUrl(), is(document.getBinaryUrl()));
        assertThat(stampDocument.getUrl(), is(document.getUrl()));
    }

    @Test
    void shouldAddStampToHighCourtDocument() throws Exception {
        Document document = document();
        byte[] imageAsBytes = service.imageAsBytes(HIGH_COURT_SEAL_PDF);

        when(evidenceManagementDownloadService.download(document.getBinaryUrl(), AUTH_TOKEN))
            .thenReturn(imageAsBytes);

        when(evidenceManagementUploadServiceService.upload(any(), anyString(), any()))
            .thenReturn(fileUploadResponse());

        Document stampDocument = service.stampDocument(document, AUTH_TOKEN, false, StampType.HIGH_COURT_STAMP, CASE_ID);

        assertThat(stampDocument, not(equalTo(imageAsBytes)));
        assertThat(stampDocument.getFileName(), is(document.getFileName()));
        assertThat(stampDocument.getBinaryUrl(), is(document.getBinaryUrl()));
        assertThat(stampDocument.getUrl(), is(document.getUrl()));
    }

    @Test
    void shouldGetImageAsBytes() throws Exception {
        byte[] imageAsBytes = service.imageAsBytes(COURT_SEAL_IMAGE);

        assertThat(imageAsBytes, notNullValue());
    }
}
