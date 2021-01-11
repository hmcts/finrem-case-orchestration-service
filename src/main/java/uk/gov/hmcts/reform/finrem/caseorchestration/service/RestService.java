package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.net.URI;
import java.util.Map;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.SERVICE_AUTHORISATION_HEADER;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class RestService {

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    public void restApiPostCall(String userAuthToken, String url, Object body) {
        restApiCall(url, buildAuthRequestWithBody(userAuthToken, body), HttpMethod.POST);
    }

    public void restApiDeleteCall(String userAuthToken, String url, Object body) {
        restApiCall(url, buildAuthRequestWithBody(userAuthToken, body), HttpMethod.DELETE);
    }

    public Object restApiGetCall(String userAuthToken, String url) {
        return restApiCall(url, buildAuthRequestWithoutBody(userAuthToken), HttpMethod.GET);
    }

    private ResponseEntity<Map> restApiCall(String url, HttpEntity<Object> request, HttpMethod httpMethod) {
        URI uri = buildUri(url);

        log.info("Making {} request to uri : {}, request : {}", httpMethod, uri, request);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                uri,
                httpMethod,
                request,
                Map.class);

            log.info("Received REST {} response: {} ", httpMethod, response);

            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    private HttpEntity<Object> buildAuthRequestWithBody(String userAuthToken, Object body) {
        return new HttpEntity<>(body, buildHeaders(userAuthToken));
    }

    private HttpEntity<Object> buildAuthRequestWithoutBody(String userAuthToken) {
        return new HttpEntity<>(buildHeaders(userAuthToken));
    }

    private HttpHeaders buildHeaders(String userAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, userAuthToken);
        headers.add(SERVICE_AUTHORISATION_HEADER, authTokenGenerator.generate());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    private URI buildUri(String url) {
        return fromHttpUrl(url).build().toUri();
    }
}
