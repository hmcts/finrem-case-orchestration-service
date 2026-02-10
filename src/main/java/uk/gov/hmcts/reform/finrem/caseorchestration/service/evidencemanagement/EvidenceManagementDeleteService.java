package uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamAuthService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.UUID;

import static java.lang.String.format;

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

    public void delete(String fileUrl, String auth) {
        if (featureToggleService.isSecureDocEnabled()) {
            deleteOnSecDoc(fileUrl, auth);
        } else {
            deleteOnDmStore(fileUrl, auth);
        }
    }

    /**
     * Deletes a document from a secure document store using the provided file URL and authentication token.
     * This method interacts with the CaseDocumentClient to execute the deletion and logs the process.
     * Handles cases where the document is not found in the store gracefully.
     *
     * <P>
     *     Note: The boolean passed to caseDocumentClient.deleteDocument() toggles hard deleting in EM.
     *     This boolean should remain false to enables EMs soft delete functionally so documents can be recovered
     * </P>
     *
     * @param fileUrl The URL of the document to be deleted.
     * @param auth The authentication token used to retrieve IdamToken and authorize the operation.
     */
    private void deleteOnSecDoc(String fileUrl, String auth) {
        IdamToken idamTokens = idamAuthService.getIdamToken(auth);
        log.info("EMSDocStore Delete file: {} and docId: {}",
            fileUrl, getDocumentIdFromFileUrl(fileUrl));
        try {
            caseDocumentClient.deleteDocument(idamTokens.getIdamOauth2Token(), idamTokens.getServiceAuthorization(),
                getDocumentIdFromFileUrl(fileUrl), Boolean.FALSE);
        } catch (FeignException.NotFound e) {
            log.warn(format(
                "Document url %s not found in document store while attempting to delete document.",
                fileUrl), e);
        }
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
