package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

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
    private final ServiceAuthTokenGenerator tokenGenerator;

    public void restApiPostCall(String userAuthToken, String url, Map<String, Object> body) {
        restTemplate.exchange(
            buildUri(url),
            HttpMethod.POST,
            buildAuthRequest(userAuthToken, body),
            Map.class);
    }

    private HttpEntity buildAuthRequest(String userAuthToken, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORISATION_HEADER, tokenGenerator.generate());
        headers.add(AUTHORIZATION_HEADER, userAuthToken);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(body, headers);
    }

    private URI buildUri(String url) {
        return fromHttpUrl(url).build().toUri();
    }
}
