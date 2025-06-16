package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.StampDocumentException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.ANNEX_IMAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.COURT_SEAL_IMAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.HIGH_COURT_SEAL_IMAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType.FAMILY_COURT_STAMP;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.StampType.HIGH_COURT_STAMP;

@ExtendWith(MockitoExtension.class)
class PdfStampingServiceTest {

    @InjectMocks
    private PdfStampingService underTest;

    @Mock
    private EvidenceManagementUploadService evidenceManagementUploadServiceService;

    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @Mock
    private DocumentConversionService documentConversionService;

    private static final byte[] ORIGINAL_PDF_BYTES = "original pdf bytes".getBytes();

    private static final byte[] STAMPED_PDF_BYTES = "stamped pdf bytes".getBytes();

    private static final byte[] FLATTEN_PDF_BYTES = "flattened pdf bytes".getBytes();

    static class StampTypeAndImageArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(FAMILY_COURT_STAMP, COURT_SEAL_IMAGE, true),
                Arguments.of(FAMILY_COURT_STAMP, COURT_SEAL_IMAGE, false),
                Arguments.of(HIGH_COURT_STAMP, HIGH_COURT_SEAL_IMAGE, true),
                Arguments.of(HIGH_COURT_STAMP, HIGH_COURT_SEAL_IMAGE, false)
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(StampTypeAndImageArgumentsProvider.class)
    void givenDocument_whenStampDocument_thenGivenCourtStampIsAddedAndPdfIsFlattened(StampType stampType,
                                                                                     String expectedImageFilename,
                                                                                     boolean isAnnexNeeded)
        throws Exception {
        // Prepare inputs
        Document document = mock(Document.class);
        when(document.getBinaryUrl()).thenReturn("some-url");
        when(document.getFileName()).thenReturn("file.pdf");

        String authToken = AUTH_TOKEN;

        when(evidenceManagementDownloadService.download("some-url", authToken))
            .thenReturn(ORIGINAL_PDF_BYTES);
        when(documentConversionService.flattenPdfDocument(eq(STAMPED_PDF_BYTES))).thenReturn(FLATTEN_PDF_BYTES);
        FileUploadResponse uploadResponse = mock(FileUploadResponse.class);
        when(uploadResponse.getStatus()).thenReturn(HttpStatus.OK);
        when(evidenceManagementUploadServiceService.upload(anyList(), eq(CASE_ID), eq(authToken)))
            .thenReturn(Collections.singletonList(uploadResponse));

        try (MockedStatic<Loader> mockedLoader = mockStatic(Loader.class);
             MockedStatic<ImageUtils> mockedImageUtils = mockStatic(ImageUtils.class)
        ) {
            PDDocument realDoc = new PDDocument();
            PDPage spyPage = spy(new PDPage());
            realDoc.addPage(spyPage);
            PDDocument spyDoc = spy(realDoc);

            mockedLoader.when(() -> Loader.loadPDF(eq(ORIGINAL_PDF_BYTES))).thenReturn(spyDoc);
            mockedImageUtils.when(() -> ImageUtils.imageAsBytes(any(String.class))).thenCallRealMethod();

            // Mock save and close behavior on PDDocument
            mockStampedPdfBytes(spyDoc);

            // Now call the public method - this will use your mocked static and mocks above
            Document result = underTest.stampDocument(document, authToken, isAnnexNeeded, stampType, CASE_ID);
            assertNotNull(result);

            // Verify uploadService called with the flattened bytes (not the original bytes)
            verify(evidenceManagementUploadServiceService).upload(argThat(list -> {
                if (list.isEmpty()) {
                    return false;
                }
                MultipartFile mf = list.getFirst();
                try {
                    return Arrays.equals(mf.getBytes(), FLATTEN_PDF_BYTES);
                } catch (Exception e) {
                    return false;
                }
            }), eq(CASE_ID), eq(authToken));
            mockedImageUtils.verify(() -> ImageUtils.imageAsBytes(expectedImageFilename));
            if (isAnnexNeeded) {
                mockedImageUtils.verify(() -> ImageUtils.imageAsBytes(ANNEX_IMAGE));
            }
        }
    }

    @Test
    void givenDocument_whenDownloadFails_thenThrowsStampDocumentException() {
        RuntimeException downloadException = new RuntimeException("Download error");

        Document mockedDocument = mock(Document.class);
        when(mockedDocument.getBinaryUrl()).thenReturn("some-url");
        when(evidenceManagementDownloadService.download(eq("some-url"), eq(AUTH_TOKEN)))
            .thenThrow(downloadException);

        StampDocumentException exception = assertThrows(StampDocumentException.class, () ->
            underTest.stampDocument(mockedDocument, AUTH_TOKEN, true, FAMILY_COURT_STAMP,
                CASE_ID)
        );
        assertStampDocumentException(exception, downloadException);
    }

    @Test
    void givenDocument_whenFlattenPdfDocumentFails_thenThrowsStampDocumentException() {
        RuntimeException flattenException = new RuntimeException("Flatten pdf failure");

        Document mockedDocument = mock(Document.class);
        when(mockedDocument.getBinaryUrl()).thenReturn("some-url");
        when(evidenceManagementDownloadService.download("some-url", AUTH_TOKEN))
            .thenReturn(ORIGINAL_PDF_BYTES);
        when(documentConversionService.flattenPdfDocument(any(byte[].class)))
            .thenThrow(flattenException);

        try (MockedStatic<Loader> mockedLoader = mockStatic(Loader.class)) {
            PDDocument realDoc = new PDDocument();
            PDPage spyPage = spy(new PDPage());
            realDoc.addPage(spyPage);
            PDDocument spyDoc = spy(realDoc);

            mockedLoader.when(() -> Loader.loadPDF(eq(ORIGINAL_PDF_BYTES))).thenReturn(spyDoc);

            StampDocumentException exception = assertThrows(StampDocumentException.class, () ->
                underTest.stampDocument(mockedDocument, AUTH_TOKEN, true, FAMILY_COURT_STAMP,
                    CASE_ID)
            );
            assertStampDocumentException(exception, flattenException);
        }
    }

    @Test
    void givenDocument_whenUploadDocumentFails_thenThrowsStampDocumentException() throws IOException {
        RuntimeException uploadException = new RuntimeException("Upload failure");

        Document mockedDocument = mock(Document.class);
        when(mockedDocument.getBinaryUrl()).thenReturn("some-url");
        when(evidenceManagementDownloadService.download("some-url", AUTH_TOKEN))
            .thenReturn(ORIGINAL_PDF_BYTES);
        when(documentConversionService.flattenPdfDocument(eq(STAMPED_PDF_BYTES))).thenReturn(FLATTEN_PDF_BYTES);
        when(evidenceManagementUploadServiceService.upload(anyList(), eq(CASE_ID), eq(AUTH_TOKEN)))
            .thenThrow(uploadException);

        try (MockedStatic<Loader> mockedLoader = mockStatic(Loader.class)) {
            PDDocument realDoc = new PDDocument();
            PDPage spyPage = spy(new PDPage());
            realDoc.addPage(spyPage);
            PDDocument spyDoc = spy(realDoc);

            mockedLoader.when(() -> Loader.loadPDF(eq(ORIGINAL_PDF_BYTES))).thenReturn(spyDoc);
            mockStampedPdfBytes(spyDoc);

            StampDocumentException exception = assertThrows(StampDocumentException.class, () ->
                underTest.stampDocument(mockedDocument, AUTH_TOKEN, true, FAMILY_COURT_STAMP,
                    CASE_ID)
            );
            assertStampDocumentException(exception, uploadException);
        }
    }

    private void mockStampedPdfBytes(PDDocument spyDoc) throws IOException {
        doAnswer(invocation -> {
            ByteArrayOutputStream os = invocation.getArgument(0);
            os.write(STAMPED_PDF_BYTES);
            return null;
        }).when(spyDoc).save(any(ByteArrayOutputStream.class));
        doNothing().when(spyDoc).close();
    }

    private void assertStampDocumentException(StampDocumentException exception,
                                              RuntimeException thrownException) {
        assertTrue(exception.getMessage().contains("Failed to annex/stamp PDF for document"));
        assertTrue(exception.getMessage().contains(thrownException.getMessage()));
        assertEquals(thrownException, exception.getCause());
    }
}
