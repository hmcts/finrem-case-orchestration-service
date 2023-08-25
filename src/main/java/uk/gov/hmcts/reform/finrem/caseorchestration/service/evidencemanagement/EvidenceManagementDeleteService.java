package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDeleteService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_HEADER = "user-id";
    private static final int DOC_UUID_LENGTH = 36;

    private final RestTemplate restTemplate;
    private final CaseDocumentClient caseDocumentClient;
    private final IdamAuthService idamAuthService;
    private final AuthTokenGenerator authTokenGenerator;
    private final FeatureToggleService featureToggleService;

    public void delete(String fileUrl, String auth) throws HttpClientErrorException {
        if (featureToggleService.isSecureDocEnabled()) {
            deleteOnSecDoc(fileUrl, auth);
        } else {
            deleteOnDmStore(fileUrl, auth);
        }
    }

    private void deleteOnSecDoc(String fileUrl, String auth) throws HttpClientErrorException {
        IdamToken idamTokens = idamAuthService.getIdamToken(auth);
        log.info("EMSDocStore Delete file: {} and docId: {}",
            fileUrl, getDocumentIdFromFileUrl(fileUrl));

        caseDocumentClient.deleteDocument(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
            getDocumentIdFromFileUrl(fileUrl), Boolean.TRUE);
    }

    private void deleteOnDmStore(String fileUrl, String authorizationToken) {

        UserDetails userDetails;
        try {
            log.info("DMStore Delete file: {} and docId: {}", fileUrl, getDocumentIdFromFileUrl(fileUrl));
            userDetails = idamAuthService.getUserDetails(authorizationToken);
        } catch (FeignException e) {
            log.info("FeignException status: {}, message: {}", e.status(), e.getMessage());
            return;
        }
        HttpEntity<Object> httpEntity = deleteServiceCallHeaders(userDetails.getId());
        ResponseEntity<String> response = restTemplate.exchange(fileUrl,
            HttpMethod.DELETE,
            httpEntity,
            String.class);
        log.debug("Document deletion response for userId {}: {}", userDetails.getId(), response);

    }

    private HttpEntity<Object> deleteServiceCallHeaders(String userId) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        httpHeaders.add(USER_ID_HEADER, userId);

        return new HttpEntity<>(httpHeaders);
    }

    private UUID getDocumentIdFromFileUrl(String fileUrl) {
        return UUID.fromString(fileUrl.substring(fileUrl.length() - DOC_UUID_LENGTH));
    }
}