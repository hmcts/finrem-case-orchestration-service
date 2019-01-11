package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PBAPaymentServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;

import java.net.URI;
import java.util.Arrays;


@Service
@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class PBAPaymentService {
    private final PBAPaymentServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    public boolean makePayment(String authToken, String pbaNumber, long amountToPay) {
        HttpEntity<String> request = buildPaymentRequest(authToken);
        URI uri = buildUri();
        log.info("Inside makePayment, PRD API uri : {}, request : {} ", uri, request);
        buildPBAAccount(pbaNumber, amountToPay);
        try {
            ResponseEntity<PaymentRequest> responseEntity = restTemplate.postForEntity(uri, uri, PaymentRequest.class);
            PaymentRequest pbaAccount = responseEntity.getBody();
            return true;
        } catch (HttpClientErrorException ex) {
            return false;
        }
    }

    private static PaymentRequest buildPBAAccount(String pbaNumber, long amountToPay) {
        FeeRequest fee = FeeRequest.builder()
                .calculatedAmount(amountToPay)
                .volume(1)
                .build();
        return PaymentRequest.builder()
                .accountNumber(pbaNumber)
                .amount(amountToPay)
                .currency("GBP")
                .service("FINREM")
                .feesList(Arrays.asList(fee))
                .build();
    }


    private HttpEntity<String> buildPaymentRequest(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        return new HttpEntity<>(headers);
    }


    private URI buildUri() {
        return UriComponentsBuilder.fromHttpUrl(
                serviceConfig.getUrl() + serviceConfig.getApi())
                .build().toUri();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Inside main");

        PaymentRequest pbaAccount = buildPBAAccount("PBA0072626", 9373);
        System.out.println("pbaAccount  = " + pbaAccount);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.convertValue(pbaAccount, JsonNode.class);
        System.out.println("jsonNode  = " + jsonNode);

    }
}
