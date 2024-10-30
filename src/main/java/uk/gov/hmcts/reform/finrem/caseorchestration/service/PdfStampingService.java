package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentStorageException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.StampDocumentException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.FinremMultipartFile;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementUploadService;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.WIDTH_AND_HEIGHT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.LETTER_DATE_FORMAT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.DocumentManagementService.CONVERTER;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfStampingService {

    public static final String APPLICATION_PDF_CONTENT_TYPE = "application/pdf";
    private final EvidenceManagementUploadService emUploadService;

    private final EvidenceManagementDownloadService emDownloadService;

    public Document stampDocument(Document document,
                                  String authToken,
                                  boolean isAnnexNeeded,
                                  StampType stampType,
                                  String caseId) {
        log.info("Stamp document : {}", document);
        try {
            byte[] docInBytes = emDownloadService.download(document.getBinaryUrl(), authToken);
            byte[] stampedDoc = stampDocument(docInBytes, isAnnexNeeded, stampType);
            MultipartFile multipartFile =
                FinremMultipartFile.builder().name(document.getFileName()).content(stampedDoc)
                    .contentType(APPLICATION_PDF_CONTENT_TYPE).build();
            List<FileUploadResponse> uploadResponse =
                emUploadService.upload(Collections.singletonList(multipartFile), caseId, authToken);
            FileUploadResponse fileSaved = Optional.of(uploadResponse.get(0))
                .filter(response -> response.getStatus() == HttpStatus.OK)
                .orElseThrow(() -> new DocumentStorageException("Failed to store document"));
            return CONVERTER.apply(fileSaved);
        } catch (Exception ex) {
            throw new StampDocumentException(format("Failed to annex/stamp PDF for document : %s, "
                + "isAnnexNeeded : %s, Exception  : %s", document, isAnnexNeeded, ex.getMessage()), ex);
        }
    }

    private byte[] stampDocument(byte[] inputDocInBytes, boolean isAnnexNeeded, StampType stampType) throws Exception {
        PDDocument doc = Loader.loadPDF(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);
        PDPage page = doc.getPage(0);
        PdfAnnexStampingInfo info = PdfAnnexStampingInfo.builder(page).build();
        log.info("PdfAnnexStampingInfo data  = {}", info);

        PDPageContentStream psdStream = new PDPageContentStream(doc, page, APPEND, true, true);

        if (StampType.FAMILY_COURT_STAMP.equals(stampType)) {
            PDImageXObject familySealImage = createFromByteArray(doc, imageAsBytes(info.getCourtSealFile()), null);
            psdStream.drawImage(familySealImage, info.getCourtSealPositionX(), info.getCourtSealPositionY(),
                WIDTH_AND_HEIGHT, WIDTH_AND_HEIGHT);
        } else if (StampType.HIGH_COURT_STAMP.equals(stampType)) {
            PDImageXObject highCourtSealImage = createFromByteArray(doc, imageAsBytes(info.getHighCourtSealFile()), null);
            psdStream.drawImage(highCourtSealImage, info.getHighCourtSealPositionX(), info.getHighCourtSealPositionY(),
                WIDTH_AND_HEIGHT, WIDTH_AND_HEIGHT);
        }
        if (isAnnexNeeded) {
            PDImageXObject annexImage = createFromByteArray(doc, imageAsBytes(info.getAnnexFile()), null);
            psdStream.drawImage(annexImage, info.getAnnexPositionX(), info.getAnnexPositionY(),
                WIDTH_AND_HEIGHT, WIDTH_AND_HEIGHT);
        }
        psdStream.close();
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        doc.save(outputBytes);
        doc.close();

        return outputBytes.toByteArray();
    }

    public Document approveDocument(Document document,
                                    String authToken,
                                    String dateTextBoxName,
                                    LocalDate approvalDate,
                                    String caseId) {
        log.info("Approve document : {}", document);
        try {
            byte[] docInBytes = emDownloadService.download(document.getBinaryUrl(), authToken);
            byte[] approvedDoc = approveDocument(docInBytes, dateTextBoxName, approvalDate);
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
            throw new StampDocumentException(format("Failed to add approved date for document : %s, "
                + "dateTextBoxName : %s, Exception  : %s", document, dateTextBoxName, ex.getMessage()), ex);
        }
    }

    private byte[] approveDocument(byte[] inputDocInBytes, String dateTextBoxName, LocalDate approvalDate) throws Exception {
        PDDocument doc = Loader.loadPDF(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);

        Optional<PDAcroForm> acroForm = Optional.ofNullable(doc.getDocumentCatalog().getAcroForm());

        if (acroForm.isPresent() && (acroForm.get().getField(dateTextBoxName) instanceof PDTextField)) {
            PDField field = acroForm.get().getField(dateTextBoxName);

                PDTextField textBox = (PDTextField) field;
                textBox.setDefaultAppearance("/Helv 12 Tf 0 g");
                textBox.setValue(approvalDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));

                ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
                doc.save(outputBytes);
                doc.close();

                return outputBytes.toByteArray();
        }

        log.info("Pdf document is flatten / not editable. Stamping Date Under Seal");
        return approveStampFlattenedDocument(doc, approvalDate);
    }


    private byte[] approveStampFlattenedDocument(PDDocument doc, LocalDate approvalDate) throws Exception {
        doc.setAllSecurityToBeRemoved(true);
        PDPage page = doc.getPage(0);
        PdfAnnexStampingInfo info = PdfAnnexStampingInfo.builder(page).build();
        log.info("PdfAnnexStampingInfo data  = {}", info);

        PDPageContentStream contentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true);

        contentStream.beginText();
        contentStream.newLineAtOffset(info.getHighCourtSealPositionX() + 20, info.getHighCourtSealPositionY() + 15);
        PDFont pdfFont = new PDType1Font(Standard14Fonts.getMappedFontName("HELVETICA").HELVETICA);
        contentStream.setFont(pdfFont, 12);
        contentStream.setNonStrokingColor(Color.red);
        contentStream.showText(approvalDate.format(DateTimeFormatter.ofPattern(LETTER_DATE_FORMAT)));
        contentStream.endText();
        contentStream.close();
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        doc.save(outputBytes);
        doc.close();
        return outputBytes.toByteArray();
    }

    public byte[] imageAsBytes(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
