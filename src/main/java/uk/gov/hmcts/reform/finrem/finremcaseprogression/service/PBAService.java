package uk.gov.hmcts.reform.finrem.finremcaseprogression.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.config.PBAServiceConfiguration;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.pba.PBAccount;

import java.net.URI;


@Service
@RequiredArgsConstructor
public class PBAService {
    private final IDAMService idamService;
    private final PBAServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    public boolean isValidPBA(String authToken, String pbaNumber) {
        String emailId = idamService.getUserEmailId(authToken);
        URI uri = buildUri(emailId);
        HttpEntity<String> request = buildAuthRequest(authToken);
        ResponseEntity<PBAccount> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, request, PBAccount.class);
        PBAccount pbAccount = responseEntity.getBody();
        return pbAccount.getAccountList().contains(pbaNumber);
    }

    private HttpEntity<String> buildAuthRequest(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(headers);
        return request;
    }

    private URI buildUri(String emailId) {
        return UriComponentsBuilder.fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi() + emailId)
                .build().toUri();
    }
}
