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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;

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

    private final IdamAuthService idamAuthService;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;
    private final FeatureToggleService featureToggleService;

    @Value("${document.management.store.baseUrl}")
    private String documentManagementStoreUrl;

    public byte[] download(String binaryFileUrl, String auth) throws HttpClientErrorException {
        if (featureToggleService.isSecureDocEnabled()) {
            return downloadFromSecDoc(binaryFileUrl, auth);
        } else {
            return downloadFromDmStore(binaryFileUrl).getBody();
        }
    }

    public ResponseEntity<Resource> downloadInResponseEntity(String binaryFileUrl, String auth) throws HttpClientErrorException {
        if (featureToggleService.isSecureDocEnabled()) {
            return downloadFromSecDocInResponseWrapper(binaryFileUrl, auth);
        } else {
            return downloadFromDmStoreInResponseWrapper(binaryFileUrl, auth);
        }
    }

    private ResponseEntity<byte[]> downloadFromDmStore(@NonNull final String binaryFileUrl) {
        log.info("DmStore Download file: {}", binaryFileUrl);
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

    private byte[] downloadFromSecDoc(String binaryFileUrl, String auth) throws HttpClientErrorException {
        log.info("EMSDocStore Download file: {}", binaryFileUrl);
        ResponseEntity<Resource> responseEntity = downloadFromSecDocInResponseWrapper(binaryFileUrl, auth);
        ByteArrayResource resource = (ByteArrayResource) responseEntity.getBody();

        return (resource != null) ? resource.getByteArray() : new byte[0];
    }

    private ResponseEntity<Resource> downloadFromDmStoreInResponseWrapper(String binaryFileUrl, String auth) throws HttpClientErrorException {
        ResponseEntity<byte[]> response = downloadFromDmStore(binaryFileUrl);
        ByteArrayResource byteArrayResource = (response.getBody() != null)
            ? new ByteArrayResource(response.getBody()) : new ByteArrayResource(new byte[0]);
        return new ResponseEntity<>(byteArrayResource, response.getStatusCode());
    }

    private ResponseEntity<Resource> downloadFromSecDocInResponseWrapper(String binaryFileUrl, String auth) throws HttpClientErrorException {

        return caseDocumentClient.getDocumentBinary(
            idamAuthService.getIdamToken(auth).getIdamOauth2Token(),
            idamAuthService.getIdamToken(auth).getServiceAuthorization(),
            binaryFileUrl);
    }
}
