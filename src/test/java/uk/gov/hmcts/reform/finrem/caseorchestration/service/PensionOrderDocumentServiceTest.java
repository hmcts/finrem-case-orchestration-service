package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.document;
import static uk.gov.hmcts.reform.finrem.caseorchestration.util.TestResource.fileUploadResponse;

@ExtendWith(MockitoExtension.class)
class PensionOrderDocumentServiceTest {

    @InjectMocks
    private PensionOrderDocumentService service;

    @Mock
   private EvidenceManagementUploadService emUploadService;
    @Mock
   private EvidenceManagementDownloadService emDownloadService;


    private String caseId = "123123123";

   private LocalDate approvalDate = LocalDate.of(2024, 11, 04);


    @Test
    void shouldUpdatePensionOrderDocument() throws IOException {
        Document document = Document.builder()
            .binaryUrl("https:mockurl/binary")
            .fileName("Testfile")
            .url("http:mockfile").build();

        byte[] docInBytes = loadResource("/fixtures/P1_pension_sharing_annex.pdf");

        when(emDownloadService.download(document.getBinaryUrl(), "auth"))
            .thenReturn(docInBytes);

        when(emUploadService.upload(any(), anyString(), any()))
            .thenReturn(fileUploadResponse());

        Document approvedAndDatedDocument = service.appendApprovedDateToDocument(document, "auth", approvalDate, caseId);

        assertThat(approvedAndDatedDocument, not(equalTo(document)));
    }

    @Test
    public void shouldAppendApprovalDateToPensionOrderDocument() throws IOException {

        Document document = document();
        LocalDate approvalDate = LocalDate.of(2024, 12, 31);

        byte[] docInBytes = loadResource("/fixtures/P1_pension_sharing_annex.pdf");
        when(emDownloadService.download(document.getBinaryUrl(), "auth"))
            .thenReturn(docInBytes);

        when(emUploadService.upload(any(), anyString(), any()))
            .thenReturn(fileUploadResponse());

        Document approvedAndDatedDocument = service.appendApprovedDateToDocument(document, "auth", approvalDate, caseId);

        PDDocument pDFdocument = Loader.loadPDF(docInBytes);
        pDFdocument.getDocumentCatalog().getAcroForm();

        Optional<PDAcroForm> acroForm = Optional.ofNullable(pDFdocument.getDocumentCatalog().getAcroForm());

        PDField field = acroForm.get().getField("Date the court made/varied/discharged an order");
        PDTextField textBox = (PDTextField) field;

        assertEquals("31 December 2024", textBox.getValueAsString());
        assertThat(approvedAndDatedDocument, not(equalTo(document)));
    }

//    void shouldNotUpdateFlattenDocument() throws IOException {
//        Document document = document();
//
//        byte[] docInBytes = loadResource("/fixtures/P1_pension_sharing_annex_flattened.pdf");
//        when(evidenceManagementDownloadService.download(any(), "auth"))
//            .thenReturn(docInBytes);
//
//        when(evidenceManagementUploadServiceService.upload(any(), anyString(), any()))
//            .thenReturn(fileUploadResponse());
//
//        Document approvedAndDatedDocument = service.appendApprovedDateToDocument(document, "auth", approvalDate, caseId);
//
//        try (PDDocument originalDoc = Loader.loadPDF(file)) {
//            PDAcroForm originalAcroForm = originalDoc.getDocumentCatalog().getAcroForm();
//            assertNotNull("Document should have an AcroForm", originalAcroForm);
//            assertFalse("Document should have form fields", originalAcroForm.getFields().isEmpty());
//        }
//        assertEquals(approvedAndDatedDocument, document);
//
//    }

    private byte[] loadResource(String testPdf) throws IOException {

        try (InputStream resourceAsStream = getClass().getResourceAsStream(testPdf)) {
            assert resourceAsStream != null;
            return resourceAsStream.readAllBytes();
        }
    }
}