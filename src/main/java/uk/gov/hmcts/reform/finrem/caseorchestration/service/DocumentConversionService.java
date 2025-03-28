package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.util.Matrix;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.DocumentConversionException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentConversionService {

    private static final String PDF_MIME_TYPE = "application/pdf";

    @Value("${service.pdf-service.uri}/rs/convert")
    private String documentConversionUrl;

    @Value("${service.pdf-service.accessKey}")
    private String docmosisAccessKey;

    private final Tika tika;

    private final RestTemplate restTemplate;

    private final EvidenceManagementDownloadService evidenceManagementService;


    public byte[] convertDocumentToPdf(Document sourceDocument, String auth) {
        if (PDF_MIME_TYPE.equalsIgnoreCase(tika.detect(sourceDocument.getFileName()))) {
            throw new DocumentConversionException(
                "Document already is a pdf",
                null
            );
        }

        return convert(sourceDocument, auth);
    }

    public String getConvertedFilename(String filename) {
        return FilenameUtils.getBaseName(filename) + ".pdf";
    }

    public byte[] flattenPdfDocument(byte[] document) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDDocument doc = null;

        try {
            doc = Loader.loadPDF(document);
            Optional<PDAcroForm> acroForm = Optional.ofNullable(doc.getDocumentCatalog().getAcroForm());

            if (acroForm.isPresent()) {
                acroForm.get().flatten();
                flattenAnnotations(doc);
                doc.save(bos);
                doc.close();
                return bos.toByteArray();
            }
        } catch (IOException e) {
            log.error("Unable to flatten document", e);
        } finally {
            IOUtils.closeQuietly(doc);
        }
        return document;
    }

    /**
     * This method iterates over all pages in the document and removes all annotations from each page.
     * This was specifically introduced to flatten unflattened signatures which caused issues printing later.
     * @param doc - The document to be flattened
     * @throws IOException - If the annotations cannot be removed
     */
    private void flattenAnnotations(PDDocument doc) throws IOException {
        for (PDPage page : doc.getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                PDAppearanceStream appearanceStream = annotation.getNormalAppearanceStream();
                if (appearanceStream != null) {
                    transformAnnotationsToContentStream(doc, page, annotation, appearanceStream);
                }
            }
            page.getAnnotations().clear();
        }
    }

    /**
     * The transformation matrix needs to be set correctly by considering both the position and the size of the
     * annotation's bounding box, along with the appearance stream's bounding box. Once the transformation matrix
     * is applied, the drawForm() method is used to draw the appearance stream directly onto the page at the
     * correct position.
     * @param doc - The document to which the annotations belong
     * @param page - The page to which the annotations belong
     * @param annotation - The annotation to be transformed
     * @param appearanceStream - The appearance stream of the annotation
     * @throws IOException - If the content stream cannot be created
     */
    private void transformAnnotationsToContentStream(PDDocument doc, PDPage page, PDAnnotation annotation,
                                                     PDAppearanceStream appearanceStream) throws IOException {
        float appearanceWidth = appearanceStream.getBBox().getWidth();
        float appearanceHeight = appearanceStream.getBBox().getHeight();

        if (appearanceWidth > 0 && appearanceHeight > 0) {
            try (PDPageContentStream contentStream =
                     new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND,
                         true, true)) {
                contentStream.saveGraphicsState();
                float x = annotation.getRectangle().getLowerLeftX();
                float y =  annotation.getRectangle().getLowerLeftY();
                float width = annotation.getRectangle().getWidth();
                float height = annotation.getRectangle().getHeight();
                Matrix transformation = new Matrix(width / appearanceWidth, 0, 0, height / appearanceHeight, x, y);
                contentStream.transform(transformation);
                contentStream.drawForm(appearanceStream);
                contentStream.restoreGraphicsState();
            }
        }
    }

    private byte[] convert(Document sourceDocument, String auth) {
        try {
            String filename = getConvertedFilename(sourceDocument.getFileName());
            byte[] docInBytes = evidenceManagementService.download(sourceDocument.getBinaryUrl(), auth);
            File file = new File(filename);
            Files.write(docInBytes, file);

            return restTemplate
                .postForObject(
                    documentConversionUrl,
                    createRequest(file, filename),
                    byte[].class
                );

        } catch (HttpClientErrorException clientEx) {

            throw new DocumentConversionException(
                "Error converting document to pdf",
                clientEx
            );
        } catch (IOException ex) {
            throw new DocumentConversionException(
                "Error creating temp file",
                ex
            );
        }
    }

    private HttpEntity<MultiValueMap<String, Object>> createRequest(
        File file,
        String outputFilename
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accessKey", docmosisAccessKey);
        body.add("outputName", outputFilename);
        body.add("file", new FileSystemResource(file));

        return new HttpEntity<>(body, headers);
    }
}
