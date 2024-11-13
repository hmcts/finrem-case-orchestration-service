package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.fileUploadResponse;

@ExtendWith(MockitoExtension.class)
class PensionAnnexDateStampServiceTest {

    @InjectMocks
    private PensionAnnexDateStampService service;
    @Mock
    private EvidenceManagementUploadService emUploadService;
    @Mock
    private EvidenceManagementDownloadService emDownloadService;
    @Mock
    private GenericDocumentService genericDocumentService;
    private final String caseId = "123123123";
    private final LocalDate approvalDate = LocalDate.of(2024, 12, 31);
    @Captor
    private ArgumentCaptor<List<MultipartFile>> filesCaptor;
    private CaseDocument document;


    @BeforeEach
    void setUp() {
        document = CaseDocument.builder()
            .documentBinaryUrl("https:mockurl/binary")
            .documentFilename("Testfile")
            .documentUrl("http:mockfile").build();
    }

    @Test
    void shouldAddApprovalDateToPensionOrderDocument() throws Exception {
        byte[] docInBytes = loadResource("/fixtures/P1_pension_sharing_annex.pdf");
        when(emDownloadService.download(document.getDocumentBinaryUrl(), AUTH_TOKEN))
            .thenReturn(docInBytes);
        when(emUploadService.upload(any(), anyString(), any()))
            .thenReturn(fileUploadResponse());
        when(genericDocumentService.toCaseDocument(any(Document.class))).thenCallRealMethod();
        service.appendApprovedDateToDocument(document, AUTH_TOKEN, approvalDate, caseId);

        verify(emUploadService).upload(filesCaptor.capture(), anyString(), anyString());
        List<MultipartFile> uploadedMultipartFiles = filesCaptor.getValue();
        byte[] bytes = uploadedMultipartFiles.get(0).getBytes();
        verifyDateOfOrderField(bytes);
    }

    @Test
    void shouldNotAddApprovalDateToPensionOrderDocumentIfApprovalDateIsMissing() {
        Exception exception = assertThrows(Exception.class, () -> service.appendApprovedDateToDocument(document, AUTH_TOKEN, null, caseId));
        assertEquals("Missing or Invalid Approved Date of Order.", exception.getMessage());
    }

    @Test
    void shouldNotAddApprovalDateToPensionOrderFlattenedDocument() throws IOException {
        byte[] docInBytes = loadResource("/fixtures/P1_pension_sharing_annex_flattened.pdf");
        when(emDownloadService.download(document.getDocumentBinaryUrl(), AUTH_TOKEN))
            .thenReturn(docInBytes);

        Exception exception = assertThrows(Exception.class, () -> service.appendApprovedDateToDocument(document, AUTH_TOKEN, approvalDate, caseId));
        assertEquals("Pension Order document PDF is flattened / not editable.", exception.getMessage());
    }

    private byte[] loadResource(String testPdf) throws IOException {
        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPdf)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }

    private void verifyDateOfOrderField(byte[] bytes) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(bytes)) {
            pdDocument.setAllSecurityToBeRemoved(true);
            PDAcroForm pdAcroForm = pdDocument.getDocumentCatalog().getAcroForm();
            PDField field = pdAcroForm.getField(PensionAnnexDateStampService.FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME);
            PDTextField textBox = (PDTextField) field;
            assertEquals("31 December 2024", textBox.getValueAsString());
        }
    }
}
