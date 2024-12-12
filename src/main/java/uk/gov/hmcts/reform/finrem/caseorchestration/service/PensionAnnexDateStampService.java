package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
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
import java.io.File;
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
    private static final String DEFAULT_PDTYPE_FONT_HELV = "/Helv 12 Tf 0 g";

    public CaseDocument appendApprovedDateToDocument(CaseDocument document,
                                                     String authToken,
                                                     LocalDate approvalDate,
                                                     String caseId) throws Exception {
        log.info("Adding date stamp to Pension Sharing Annex for Case id: {}", caseId);
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
                .orElseThrow(() -> new DocumentStorageException("Failed to store document Case id: " + caseId));
            Document dateStampedDocument = CONVERTER.apply(fileSaved);
            return genericDocumentService.toCaseDocument(dateStampedDocument);
        } else {
            throw new StampDocumentException("Missing or Invalid Approved Date of Order for Case id: " + caseId);
        }
    }

    private byte[] appendApprovedDateToDocument(byte[] inputDocInBytes, LocalDate approvalDate) throws StampDocumentException, IOException {
        PDDocument doc = Loader.loadPDF(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        if (acroForm != null && acroForm.getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME) != null
            && (acroForm.getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME) instanceof PDTextField)) {

            // Update the default appearance string
            PDField field = acroForm.getField(FORM_P1_DATE_OF_ORDER_TEXTBOX_NAME);
            PDTextField textBox = (PDTextField) field;
            String defaultAppearance = textBox.getDefaultAppearance();
            String fontName = getFontNameFromDA(defaultAppearance);

            try {
                textBox.setDefaultAppearance(defaultAppearance);
                textBox.setValue(approvalDate.format(DateTimeFormatter.ofPattern(DATE_STAMP_PATTERN).withLocale(Locale.UK)));
            } catch (IOException ioException) {
                log.error("Failed to update PDF when attempting to use font 'Helvetica' that isn't embedded, Exception: " + ioException);
            } finally {
                // Resolve and embed the font if missing
                PDFont font = resolveFont(acroForm, fontName, doc, new File(getClass().getClassLoader().getResource("Helvetica.ttf").getFile()));
                log.info("Finally resolve font and retrying with font {}: ", font.getName());
                textBox.setDefaultAppearance(DEFAULT_PDTYPE_FONT_HELV);
                textBox.setValue(approvalDate.format(DateTimeFormatter.ofPattern(DATE_STAMP_PATTERN).withLocale(Locale.UK)));
            }

            ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
            doc.save(outputBytes);
            doc.close();
            return outputBytes.toByteArray();
        } else {
            throw new StampDocumentException("Pension Order document PDF is flattened / not editable.");
        }
    }

    private PDFont resolveFont(PDAcroForm acroForm, String fontName, PDDocument document, File fontFilePath) throws IOException {
        if (fontName == null) {
            return null;
        }
        // Check if the font exists in the default resources
        PDResources resources = acroForm.getDefaultResources();
        if (resources != null) {
            PDFont font = resources.getFont(COSName.getPDFName(fontName));
            if (font != null) {
                log.info("Font found in resources: " + fontName);
                return font;
            }
        }
        // Load and embed the fallback font if not found
        log.info("Font not found in resources. Embedding fallback font...");
        PDType0Font fallbackFont = PDType0Font.load(document, fontFilePath);
        // Add fallback font to default resources
        if (resources == null) {
            resources = new PDResources();
            acroForm.setDefaultResources(resources);
        }
        resources.put(COSName.getPDFName("Helvetica"), fallbackFont);
        // Update Default Appearance String
        acroForm.setDefaultAppearance(DEFAULT_PDTYPE_FONT_HELV);
        return fallbackFont;
    }

    private String getFontNameFromDA(String defaultAppearance) {
        if (defaultAppearance != null && !defaultAppearance.isEmpty()) {
            String[] parts = defaultAppearance.split("\\s+");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].startsWith("/")) {
                    return parts[i].substring(1); // Strip leading '/'
                }
            }
        }
        return null;
    }
}
