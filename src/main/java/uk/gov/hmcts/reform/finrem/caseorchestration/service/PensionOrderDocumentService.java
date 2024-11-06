package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.StampDocumentException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.FinremMultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentManagementService.CONVERTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class PensionOrderDocumentService {

    public static final String APPLICATION_PDF_CONTENT_TYPE = "application/pdf";
    private final EvidenceManagementUploadService emUploadService;

    private final EvidenceManagementDownloadService emDownloadService;

    private static final String FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME = "Date the court made/varied/discharged an order";

    public Document appendApprovedDateToDocument(Document document,
                                                 String authToken,
                                                 LocalDate approvalDate,
                                                 String caseId) {
        log.info("Adding date stamp to Pension Order document : {}", document);
        try {
            byte[] docInBytes = emDownloadService.download(document.getBinaryUrl(), authToken);
            byte[] approvedDoc = appendApprovedDateToDocument(docInBytes, approvalDate);
            MultipartFile multipartFile =
                FinremMultipartFile.builder().name(document.getFileName()).content(approvedDoc)
                    .contentType(APPLICATION_PDF_CONTENT_TYPE).build();
            List<FileUploadResponse> uploadResponse =
                emUploadService.upload(Collections.singletonList(multipartFile), caseId, authToken);
            FileUploadResponse fileSaved = Optional.of(uploadResponse.get(0))
                .filter(response -> response.getStatus() == HttpStatus.OK)
                .orElseThrow(() -> new DocumentStorageException("Failed to store document"));
            return CONVERTER.apply(fileSaved);
        } catch (Exception ex) {
            throw new StampDocumentException(format("Failed to add date stamp to Pension Order document : %s, "
                + ", Exception  : %s", document, ex.getMessage()), ex);
        }
    }

    public byte[] appendApprovedDateToDocument(byte[] inputDocInBytes, LocalDate approvalDate) throws Exception {
        PDDocument doc = Loader.loadPDF(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);

        Optional<PDAcroForm> acroForm = Optional.ofNullable(doc.getDocumentCatalog().getAcroForm());

        if (acroForm.isPresent() && (acroForm.get().getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME) instanceof PDTextField)) {
            PDField field = acroForm.get().getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME);

            PDTextField textBox = (PDTextField) field;
            textBox.setDefaultAppearance("/Helv 12 Tf 0 g");
            textBox.setValue(approvalDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));

            ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
            doc.save(outputBytes);
            doc.close();

            return outputBytes.toByteArray();
        }

        log.info("Unable to append Date of Order. Pension Order document PDF is flattened / not editable.");
        return inputDocInBytes;
    }
}
