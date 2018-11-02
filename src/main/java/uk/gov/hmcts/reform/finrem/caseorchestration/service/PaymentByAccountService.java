package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PaymentByAccountServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.PaymentByAccount;

import java.net.URI;


@Service
@RequiredArgsConstructor
public class PaymentByAccountService {
    private final IdamService idamService;
    private final PaymentByAccountServiceConfiguration paymentByAccountServiceConfiguration;
    private final RestTemplate restTemplate;

    public boolean isValidPBA(String authToken, String pbaNumber) {
        String emailId = idamService.getUserEmailId(authToken);
        URI uri = buildUri(emailId);
        HttpEntity<String> request = buildAuthRequest(authToken);
        ResponseEntity<PaymentByAccount> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, request,
                PaymentByAccount.class);
        PaymentByAccount paymentByAccount = responseEntity.getBody();
        return paymentByAccount.getAccountList().contains(pbaNumber);
    }

    private HttpEntity<String> buildAuthRequest(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }

    private URI buildUri(String emailId) {
        return UriComponentsBuilder.fromHttpUrl(
                paymentByAccountServiceConfiguration.getUrl() + paymentByAccountServiceConfiguration.getApi() + emailId)
                .build().toUri();
    }
}
