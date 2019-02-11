package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PBAValidationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAAccount;

import java.net.URI;


@Service
@RequiredArgsConstructor
@Slf4j
public class PBAValidationService {
    private final IdamService idamService;
    private final PBAValidationServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    public boolean isValidPBA(String authToken, String pbaNumber) {
        String emailId = idamService.getUserEmailId(authToken);
        URI uri = buildUri(emailId);
        log.info("Inside isValidPBA, PRD API uri : {}, emailId : {}", uri, emailId);
        try {
            HttpEntity request = buildRequest(authToken);
            ResponseEntity<PBAAccount> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, request,
                    PBAAccount.class);
            PBAAccount pbaAccount = responseEntity.getBody();
            return pbaAccount.getAccountList().contains(pbaNumber);
        } catch (HttpClientErrorException ex) {
            return false;
        }
    }

    private HttpEntity buildRequest(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }


    private URI buildUri(String emailId) {
        return UriComponentsBuilder.fromHttpUrl(
                serviceConfig.getUrl() + serviceConfig.getApi() + emailId)
                .build().toUri();
    }
}
