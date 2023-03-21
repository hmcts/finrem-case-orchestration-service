package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementUploadService {

    protected static final String JURISDICTION_ID = "DIVORCE";
    private final CaseDocumentClient caseDocumentClient;
    private final IdamAuthService idamAuthService;

    public List<FileUploadResponse> upload(List<MultipartFile> files, String caseTypeId, String auth)
        throws HttpClientErrorException {
        IdamToken idamTokens = idamAuthService.getIdamToken(auth);
        log.info("EMSDocStore Upload files: {} with user: {} and case id: {}",
            files.toString(), idamTokens.getEmail(), caseTypeId);

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), caseTypeId, JURISDICTION_ID, files);

        if (uploadResponse == null) {
            log.info("EMSDocStore Failed to upload files");
            return null; // TODO: refactor to return empty list instead
        }

        log.info("EMSDocStore Uploaded files are: {} with user: {} and case id: {}",
            uploadResponse.getDocuments().stream().map(e -> e.links.binary.href).collect(Collectors.toList()),
            idamTokens.getEmail(), caseTypeId);

        return toUploadResponse(uploadResponse);
    }

    private List<FileUploadResponse> toUploadResponse(UploadResponse uploadResponse) {
        Stream<Document> documentStream = stream(uploadResponse.getDocuments().spliterator(), false);

        return documentStream.map(this::createUploadResponse).collect(Collectors.toList());
    }

    private FileUploadResponse createUploadResponse(Document document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(document.links.self.href)
            .fileName(document.originalDocumentName)
            .mimeType(document.mimeType)
            .createdBy(document.createdBy)
            .createdOn(getLocalDateTime(document.createdOn))
            .lastModifiedBy(document.lastModifiedBy)
            .modifiedOn(getLocalDateTime(document.modifiedOn))
            .build();
    }

    private String getLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();

        return ldt.toString();
    }

}
