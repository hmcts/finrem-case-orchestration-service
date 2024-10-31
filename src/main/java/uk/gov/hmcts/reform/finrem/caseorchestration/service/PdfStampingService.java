package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.PdfAnnexStampingInfo.WIDTH_AND_HEIGHT;
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

    public byte[] imageAsBytes(String fileName) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
