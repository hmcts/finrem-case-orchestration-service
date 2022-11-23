package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.evidence.FileUploadResponse;
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

    private final RestTemplate restTemplate;
    private final IdamAuthService idamAuthService;
    private final AuthTokenGenerator authTokenGenerator;


    public List<FileUploadResponse> audit(List<String> fileUrls, String authorizationToken) {
        log.info("Audit requested for documents: fileUrls={}", fileUrls);

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

    private FileUploadResponse createUploadResponse(JsonNode document) {
        return FileUploadResponse.builder()
            .status(HttpStatus.OK)
            .fileUrl(new HalLinkDiscoverer()
                .findLinkWithRel("self", document.toString())
                .orElseThrow(() -> new IllegalStateException("self rel link not found"))
                .getHref())
            .fileName(document.get("originalDocumentName").asText())
            .createdBy(document.get("createdBy").asText())
            .createdOn(document.get("createdOn").asText())
            .lastModifiedBy(document.get("lastModifiedBy") != null ? document.get("lastModifiedBy").asText() : "")
            .modifiedOn(document.get("modifiedOn").asText())
            .mimeType(document.get("mimeType").asText())
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
