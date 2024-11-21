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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentManagementService.CONVERTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class PensionAnnexDateStampService {
    private final EvidenceManagementUploadService emUploadService;
    private final EvidenceManagementDownloadService emDownloadService;
    private final GenericDocumentService genericDocumentService;
    static final String FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME = "Date the court made/varied/discharged an order";
    static final String DATE_STAMP_PATTERN = "dd MMMM yyyy";
    static final String DEFAULT_PDTYPE_FONT_HELV = "Helv";
    static final String DEFAULT_PDTYPE_PREFIX = "/";
    static final String DEFAULT_PDTYPE_POSTFIX = " 10 Tf 0 g";

    public CaseDocument appendApprovedDateToDocument(CaseDocument document,
                                                     String authToken,
                                                     LocalDate approvalDate,
                                                     String caseId) throws Exception {
        log.info("Adding date stamp to Pension Sharing Annex : {}", document);
        Optional<LocalDate> optionalApprovalDate = Optional.ofNullable(approvalDate);
        if (optionalApprovalDate.isPresent()) {
            byte[] docInBytes = emDownloadService.download(document.getDocumentBinaryUrl(), authToken);
            byte[] approvedDoc = appendApprovedDateToDocument(docInBytes, approvalDate);
            MultipartFile multipartFile =
                FinremMultipartFile.builder().name(document.getDocumentFilename()).content(approvedDoc)
                    .contentType(APPLICATION_PDF_VALUE).build();
            List<FileUploadResponse> uploadResponse =
                emUploadService.upload(Collections.singletonList(multipartFile), caseId, authToken);
            FileUploadResponse fileSaved = Optional.of(uploadResponse.get(0))
                .filter(response -> response.getStatus() == HttpStatus.OK)
                .orElseThrow(() -> new DocumentStorageException("Failed to store document"));
            Document dateStampedDocument = CONVERTER.apply(fileSaved);
            return genericDocumentService.toCaseDocument(dateStampedDocument);
        } else {
            throw new StampDocumentException("Missing or Invalid Approved Date of Order.");
        }
    }

    private byte[] appendApprovedDateToDocument(byte[] inputDocInBytes, LocalDate approvalDate) throws StampDocumentException, IOException {
        PDDocument doc = Loader.loadPDF(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);
        Optional<PDAcroForm> acroForm = Optional.ofNullable(doc.getDocumentCatalog().getAcroForm());
        if (acroForm.isPresent() && acroForm.get().getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME) != null
            && (acroForm.get().getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME) instanceof PDTextField)) {
            PDField field = acroForm.get().getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME);
            PDTextField textBox = (PDTextField) field;
            if (textBox.getDefaultAppearance() == null && textBox.getDefaultAppearance().isEmpty()) {
                textBox.setDefaultAppearance(DEFAULT_PDTYPE_PREFIX + DEFAULT_PDTYPE_FONT_HELV + DEFAULT_PDTYPE_POSTFIX);
            }
            textBox.setValue(approvalDate.format(DateTimeFormatter.ofPattern(DATE_STAMP_PATTERN).withLocale(Locale.UK)));
            ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
            doc.save(outputBytes);
            doc.close();
            return outputBytes.toByteArray();
        } else {
            throw new StampDocumentException("Pension Order document PDF is flattened / not editable.");
        }
    }
}
