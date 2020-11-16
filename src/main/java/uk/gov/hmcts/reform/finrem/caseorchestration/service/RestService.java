package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
public class RestService {

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    public void restApiPostCall(String userAuthToken, String url, Object body) {
        URI uri = buildUri(url);
        log.info("restApiPostCall - uri - {}", uri.toString());
        HttpEntity authRequest = buildAuthRequest(userAuthToken, body);
        log.info("restApiPostCall - authRequest - {}", authRequest.toString());

        try {
            restTemplate.exchange(
                uri,
                HttpMethod.POST,
                authRequest,
                Map.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private HttpEntity<Object> buildAuthRequest(String userAuthToken, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, userAuthToken);
        headers.add(SERVICE_AUTHORISATION_HEADER, authTokenGenerator.generate());
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(body, headers);
    }

    private URI buildUri(String url) {
        return fromHttpUrl(url).build().toUri();
    }
}
