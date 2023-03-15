package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.InvalidUriException;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDownloadService {

    private static final String FINANCIAL_REMEDY_COURT_ADMIN = "caseworker-divorce-financialremedy-courtadmin";
    private static final String USER_ROLES = "user-roles";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    public ResponseEntity<byte[]> download(@NonNull final String binaryFileUrl) {
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

    private String getUrl(String binaryFileUrl) throws URISyntaxException {
        return documentManagementStoreUrl + new URI(binaryFileUrl).getPath();
    }
}
