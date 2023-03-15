package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidUriException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDownloadService {

    private final CaseDocumentClient caseDocumentClient;
    private static final String FINANCIAL_REMEDY_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";
    private static final String USER_ROLES = "user-roles";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    public ResponseEntity<byte[]> download(@NonNull final String binaryFileUrl, String authorizationToken) {
        log.info("Binary url for file download : {} ", binaryFileUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.set(USER_ROLES, FINANCIAL_REMEDY_COURT_ADMIN);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        String url;
        try {
            url = getUrl(binaryFileUrl);
        } catch (URISyntaxException e) {
            log.error("Failed to rewrite the url for document for {}, error message {}", binaryFileUrl, e.getMessage());
            throw new InvalidUriException(format("Failed to rewrite the url for document for %s and error %s",
                    binaryFileUrl, e.getMessage()));
        }

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get bytes from document store for document {} ", binaryFileUrl);
            throw new RuntimeException(format("Unexpected code from DM store: %s ", response.getStatusCode()));
        }

        log.info("File download status : {} ", response.getStatusCode());
        return response;
    }

    public byte[] download(String binaryFileUrl, IdamToken idamTokens) throws HttpClientErrorException {
        ResponseEntity<Resource> responseEntity = downloadResource(binaryFileUrl, idamTokens);
        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();

        return (resource != null) ? resource.getByteArray() : new byte[0];
    }

    private ResponseEntity<Resource> downloadResource(String binaryFileUrl, IdamToken idamTokens) {
        String documentHref = URI.create(binaryFileUrl).getPath().replaceFirst("/", "");
        log.info("EMSDocStore Download file: {} with user: {}", documentHref, idamTokens.getEmail());

        return caseDocumentClient.getDocumentBinary(idamTokens.getIdamOauth2Token(),
            idamTokens.getServiceAuthorization(), documentHref);
    }

    private String getUrl(String binaryFileUrl) throws URISyntaxException {
        return documentManagementStoreUrl + new URI(binaryFileUrl).getPath();
    }
}
