package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;


@Service
@RequiredArgsConstructor
@Slf4j
public class EvidenceManagementDeleteService {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String USER_ID_HEADER = "user-id";

    private final RestTemplate restTemplate;
    private final IdamAuthService idamAuthService;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * This method attempts to delete the document stored in the Evidence Management document store identified by the
     * given file url.
     * <p/>
     *
     * @param fileUrl            a String containing the access details of the file to be deleted
     * @param authorizationToken a String holding the authorisation token of the current user
     * @return a ResponseEntity instance containing the response received from the Evidence Management service
     */
    public ResponseEntity<String> deleteFile(String fileUrl,
                                        String authorizationToken) {

        UserDetails userDetails;
        try {
            userDetails = idamAuthService.getUserDetails(authorizationToken);
        } catch (FeignException e) {
            log.info("FeignException status: {}, message: {}", e.status(), e.getMessage());
            return new ResponseEntity<>(e.contentUTF8(), HttpStatus.valueOf(e.status()));
        }
        HttpEntity<Object> httpEntity = deleteServiceCallHeaders(userDetails.getId());
        ResponseEntity<String> response = restTemplate.exchange(fileUrl,
                HttpMethod.DELETE,
                httpEntity,
                String.class);
        log.debug("Document deletion response for userId {}: {}", userDetails.getId(), response);

        return response;
    }


    /**
     * This method generates the http headers required to be provided as part of the delete document request.
     * <p/>
     *
     * @param userId a String holding the userId of the current user
     * @return an HttpEntity instance holding the formatted headers
     */

    private HttpEntity<Object> deleteServiceCallHeaders(String userId) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
        httpHeaders.add(USER_ID_HEADER, userId);

        return new HttpEntity<>(httpHeaders);
    }
}