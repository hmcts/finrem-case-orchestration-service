package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementAuditService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_HEADER = "user-id";
    private static final String USER_ROLES_HEADER = "user-roles";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private final IdamAuthService idamAuthService;
    private final AuthTokenGenerator authTokenGenerator;
    private final FeatureToggleService featureToggleService;
    private final RestTemplate restTemplate;
    private final CaseDocumentClient caseDocumentClient;

    public List<FileUploadResponse> audit(List<String> fileUrls, String auth) {
        if (featureToggleService.isSecureDocEnabled()) {
            return auditSecDoc(fileUrls, auth);
        } else {
            return auditDmStore(fileUrls, auth);
        }
    }

    @SuppressWarnings("java:S3740")
    private List<FileUploadResponse> auditDmStore(List<String> fileUrls, String authorizationToken) {
        log.info("DMStore audit requested for documents: fileUrls={}", fileUrls);

        UserDetails userDetails = idamAuthService.getUserDetails(authorizationToken);
        HttpEntity httpEntity = new HttpEntity(headers(userDetails));

        List<FileUploadResponse> filesAuditDetails = new ArrayList<>();
        fileUrls.forEach(fileUrl -> {
            JsonNode document = restTemplate.exchange(
                fileUrl,
                HttpMethod.GET,
                httpEntity,
                JsonNode.class).getBody();

            FileUploadResponse fileAuditResponse = createUploadResponse(document);
            filesAuditDetails.add(fileAuditResponse);

            log.info("Audit for {}: {}", fileUrl, fileAuditResponse);
        });

        return filesAuditDetails;
    }

    private List<FileUploadResponse> auditSecDoc(List<String> fileUrls, String auth) {
        log.info("EMSDocStore audit requested for documents: fileUrls={}", fileUrls);

        String idamToken = idamAuthService.getIdamToken(auth).getIdamOauth2Token();
        String serviceToken = idamAuthService.getIdamToken(auth).getServiceAuthorization();

        List<FileUploadResponse> filesAuditDetails = new ArrayList<>();
        fileUrls.forEach(fileUrl -> {
            Document document = caseDocumentClient.getMetadataForDocument(
                idamToken,
                serviceToken,
                fileUrl);

            FileUploadResponse fileAuditResponse = createUploadResponse(document);
            filesAuditDetails.add(fileAuditResponse);

            log.info("Audit for {}: {}", fileUrl, fileAuditResponse);
        });

        return filesAuditDetails;
    }

    private FileUploadResponse createUploadResponse(JsonNode document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(new HalLinkDiscoverer()
                .findLinkWithRel("self", document.toString())
                .orElseThrow(() -> new IllegalStateException("self rel link not found"))
                .getHref())
            .fileName(document.get("originalDocumentName").asText())
            .createdBy(document.get("createdBy") != null ? document.get("createdBy").asText() : "")
            .createdOn(document.get("createdOn").asText())
            .lastModifiedBy(document.get("lastModifiedBy") != null ? document.get("lastModifiedBy").asText() : "")
            .modifiedOn(document.get("modifiedOn").asText())
            .mimeType(document.get("mimeType").asText())
            .build();
    }

    private FileUploadResponse createUploadResponse(Document document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(document.links.self.href)
            .fileName(document.originalDocumentName)
            .createdBy(document.createdBy)
            .createdOn(DateFormatUtils.format(document.createdOn, DATE_FORMAT))
            .lastModifiedBy(document.lastModifiedBy)
            .modifiedOn(DateFormatUtils.format(document.modifiedOn, DATE_FORMAT))
            .mimeType(document.mimeType)
            .build();
    }

    private HttpHeaders headers(UserDetails userDetails) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        headers.set(USER_ID_HEADER, userDetails.getId());
        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            headers.set(USER_ROLES_HEADER, String.join(",", userDetails.getRoles()));
        }

        return headers;
    }
}
