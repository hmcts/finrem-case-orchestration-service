package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.builder.UploadRequestBuilder.param;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementUploadService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    protected static final String JURISDICTION_ID = "DIVORCE";

    private final RestTemplate template;
    private final CaseDocumentClient caseDocumentClient;
    private final IdamAuthService idamAuthService;
    private final AuthTokenGenerator authTokenGenerator;
    private final FeatureToggleService featureToggleService;

    @Value("${document.management.store.upload.url}")
    private String documentManagementStoreUploadUrl;

    public List<FileUploadResponse> upload(List<MultipartFile> files, String caseTypeId, String auth) {
        if (featureToggleService.isSecureDocEnabled()) {
            return uploadToSecDoc(files, caseTypeId, auth);
        } else {
            return uploadToDmStore(files, caseTypeId, auth);
        }
    }

    private List<FileUploadResponse> uploadToDmStore(@NonNull final List<MultipartFile> files, final String caseTypeId, final String auth) {
        UserDetails userDetails = idamAuthService.getUserDetails(auth);
        IdamToken idamTokens = idamAuthService.getIdamToken(auth);
        log.info("DMStore Upload files: {} with user: {} and case id: {}", files.toString(), idamTokens.getEmail(), caseTypeId);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param(files), headers(userDetails.getId()));

        JsonNode documents = Objects.requireNonNull(template.postForObject(documentManagementStoreUploadUrl, httpEntity, ObjectNode.class))
            .path("_embedded").path("documents");

        return toUploadResponse(documents);
    }

    private List<FileUploadResponse> uploadToSecDoc(List<MultipartFile> files, String caseTypeId, String auth)
        throws HttpClientErrorException {
        IdamToken idamTokens = idamAuthService.getIdamToken(auth);
        log.info("EMSDocStore Upload files: {} with user: {} and case id: {}",
            files.toString(), idamTokens.getEmail(), caseTypeId);

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), caseTypeId, JURISDICTION_ID, files);

        if (uploadResponse == null) {
            log.info("EMSDocStore Failed to upload files");
            return List.of();
        }

        log.info("EMSDocStore Uploaded files are: {} with user: {} and case id: {}",
            uploadResponse.getDocuments().stream().map(e -> e.links.binary.href).collect(Collectors.toList()),
            idamTokens.getEmail(), caseTypeId);

        return toUploadResponse(uploadResponse);
    }

    private FileUploadResponse createUploadResponse(JsonNode document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(new HalLinkDiscoverer()
                .findLinkWithRel("self", document.toString())
                .orElseThrow(() -> new IllegalStateException("self rel link not found"))
                .getHref())
            .fileName(document.get("originalDocumentName").asText())
            .createdBy(getTextFromJsonNode(document, "createdBy"))
            .createdOn(document.get("createdOn").asText())
            .lastModifiedBy(getTextFromJsonNode(document, "lastModifiedBy"))
            .modifiedOn(getTextFromJsonNode(document, "modifiedOn"))
            .mimeType(document.get("mimeType").asText())
            .build();
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

    private List<FileUploadResponse> toUploadResponse(JsonNode documents) {
        Stream<JsonNode> filesStream = stream(documents.spliterator(), false);

        return filesStream
            .map(this::createUploadResponse)
            .collect(Collectors.toList());
    }

    private List<FileUploadResponse> toUploadResponse(UploadResponse uploadResponse) {
        Stream<Document> documentStream = stream(uploadResponse.getDocuments().spliterator(), false);

        return documentStream.map(this::createUploadResponse).collect(Collectors.toList());
    }

    private String getTextFromJsonNode(JsonNode document, String attribute) {
        return Optional.ofNullable(document)
            .flatMap(file -> Optional.ofNullable(attribute).map(file::asText))
            .orElse(null);
    }

    private HttpHeaders headers(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set("Content-Type", "multipart/form-data");
        headers.set("user-id", userId);

        return headers;
    }

    private String getLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime ldt = instant.atOffset(ZoneOffset.UTC).toLocalDateTime();

        return ldt.toString();
    }

}
