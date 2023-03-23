package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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

    public Document stampDocument(Document document, String authToken, boolean isAnnexNeeded) {
        log.info("Stamp document : {}", document);
        try {
            byte[] docInBytes = emDownloadService.download(document.getBinaryUrl()).getBody();
            byte[] stampedDoc = stampDocument(docInBytes, isAnnexNeeded);
            MultipartFile multipartFile =
                FinremMultipartFile.builder().name(document.getFileName()).content(stampedDoc)
                    .contentType(APPLICATION_PDF_CONTENT_TYPE).build();
            List<FileUploadResponse> uploadResponse = emUploadService.upload(Collections.singletonList(multipartFile), authToken);
            FileUploadResponse fileSaved = Optional.of(uploadResponse.get(0))
                .filter(response -> response.getStatus() == HttpStatus.OK)
                .orElseThrow(() -> new DocumentStorageException("Failed to store document"));
            return CONVERTER.apply(fileSaved);
        } catch (Exception ex) {
            throw new StampDocumentException(format("Failed to annex/stamp PDF for document : %s, "
                + "isAnnexNeeded : %s, Exception  : %s", document, isAnnexNeeded, ex.getMessage()), ex);
        }
    }

    private byte[] stampDocument(byte[] inputDocInBytes, boolean isAnnexNeeded) throws Exception {
        PDDocument doc = PDDocument.load(inputDocInBytes);
        doc.setAllSecurityToBeRemoved(true);
        PDPage page = doc.getPage(0);
        PdfAnnexStampingInfo info = PdfAnnexStampingInfo.builder(page).build();
        log.info("PdfAnnexStampingInfo data  = {}", info);

        PDImageXObject annexImage = createFromByteArray(doc, imageAsBytes(info.getAnnexFile()), null);
        PDImageXObject courtSealImage = createFromByteArray(doc, imageAsBytes(info.getCourtSealFile()), null);
        PDPageContentStream psdStream = new PDPageContentStream(doc, page, APPEND, true, true);
        psdStream.drawImage(courtSealImage, info.getCourtSealPositionX(), info.getCourtSealPositionY(),
            WIDTH_AND_HEIGHT, WIDTH_AND_HEIGHT);
        if (isAnnexNeeded) {
            psdStream.drawImage(annexImage, info.getAnnexPositionX(), info.getAnnexPositionY(),
                WIDTH_AND_HEIGHT, WIDTH_AND_HEIGHT);
        }
        psdStream.close();
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        doc.save(outputBytes);
        doc.close();

        return outputBytes.toByteArray();
    }

    public byte[] imageAsBytes(String fileName) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(fileName)) {
            return IOUtils.toByteArray(inputStream);
        }
    }
}
