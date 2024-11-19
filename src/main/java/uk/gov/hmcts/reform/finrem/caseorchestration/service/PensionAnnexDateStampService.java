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
import java.util.stream.StreamSupport;

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
    static final String DEFAULT_PDTYPE_FONT_ARIAL = "/Arial 12 Tf 0 g";
    static final String DEFAULT_PDTYPE_PREFIX = "/";
    static final String DEFAULT_PDTYPE_POSTFIX = " 12 Tf 0 g";
    static PDResources pdResources = null;
    static PDDocument document = null;

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
            try {
                textBox.setValue(approvalDate.format(DateTimeFormatter.ofPattern(DATE_STAMP_PATTERN).withLocale(Locale.UK)));
            } catch (IOException ex) {
                textBox.setDefaultAppearance(getDefaultFont(acroForm.get()));
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

    private String getDefaultFont(PDAcroForm acroForm) {
        pdResources = acroForm.getDefaultResources();
        return StreamSupport.stream(pdResources.getFontNames().spliterator(), false)
            .map(cosName -> {
                try {
                    PDFont font = pdResources.getFont(cosName);
                    if (font != null && font.isEmbedded()) {
                        return DEFAULT_PDTYPE_PREFIX + cosName.getName() + DEFAULT_PDTYPE_POSTFIX;
                    }
                } catch (IOException e) {
                    try {
                        setDefaultEmbeddedFont();
                    } catch (IOException ex) {
                        throw new StampDocumentException("PDF Document is missing embedded font.",ex);
                    }
                    return DEFAULT_PDTYPE_FONT_ARIAL;
                }
                return null;
            })
            .filter(result -> result != null)
            .findFirst()
            .orElse(DEFAULT_PDTYPE_FONT_ARIAL);
    }

    private void setDefaultEmbeddedFont() throws IOException {
        COSName fontName = COSName.getPDFName("Arial");
        if (pdResources.getFont(fontName) == null) {
            PDType0Font font = PDType0Font.load(document, new File("Arial.ttf"));
            pdResources.put(fontName, font);
        }
    }
}
