package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentGeneratorValidationService {

    private final EvidenceManagementDownloadService evidenceManagementDownloadService;
    private final Tika tika;

    @Value("#{'${document.validation.mimeTypes}'.split(',')}")
    private List<String> mimeTypes;

    @Value("${document.validation.fileUploadErrorMessage}")
    private String fileUploadErrorMessage;

    public DocumentValidationResponse validateFileType(String fileBinaryUrl, String auth) {
        byte[] file = evidenceManagementDownloadService.download(fileBinaryUrl, auth);
        DocumentValidationResponse.DocumentValidationResponseBuilder builder =
            DocumentValidationResponse.builder();
        if (Objects.isNull(file)) {
            builder.errors(singletonList("Downloaded document is empty"));
        } else {
            try (InputStream targetStream = new ByteArrayInputStream(file)) {
                String detect = tika.detect(targetStream, new Metadata());
                if (mimeTypes.contains(detect)) {
                    builder.mimeType(detect);
                } else {
                    builder.errors(singletonList(fileUploadErrorMessage)).mimeType(detect);
                }
            } catch (IOException ex) {
                log.error("Unable to detect the MimeType due to IOException", ex.getMessage());
                builder.errors(singletonList("Unable to detect the MimeType due to IOException"));
            }
        }

        return builder.build();
    }
}
