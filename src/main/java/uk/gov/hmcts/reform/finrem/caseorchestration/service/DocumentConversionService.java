package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
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
            doc = PDDocument.load(document);
            Optional<PDAcroForm> acroForm = Optional.ofNullable(doc.getDocumentCatalog().getAcroForm());

            if (acroForm.isPresent()) {
                acroForm.get().flatten();
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
