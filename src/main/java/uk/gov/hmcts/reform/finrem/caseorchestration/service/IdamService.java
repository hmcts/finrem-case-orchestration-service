package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.IdamServiceConfiguration;

import java.net.URI;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class IdamService {
    private final IdamServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    public String getUserEmailId(String authToken) {
        HttpEntity<String> request = buildAuthRequest(authToken);
        URI uri = buildUri();
        log.info("Inside getUserEmailId, IDAM API uri : {}, request : {} ", uri, request);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, request, Map.class);
        Map result = responseEntity.getBody();
        return result.get("email").toString();
    }

    private HttpEntity<String> buildAuthRequest(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        return new HttpEntity<>(headers);
    }

    private URI buildUri() {
        return UriComponentsBuilder.fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi())
                .build().toUri();
    }
}
