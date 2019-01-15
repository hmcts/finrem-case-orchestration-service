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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class PBAPaymentService {
    private final PBAPaymentServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;
    private static final ObjectMapper mapper = new ObjectMapper();

    public PaymentResponse makePayment(String authToken, CaseData caseData) throws Exception {
        PaymentRequest paymentRequest = buildPaymentRequest(caseData);
        HttpEntity<PaymentRequest> request = buildPaymentRequest(authToken, paymentRequest);
        URI uri = buildUri();
        log.info("Inside makePayment, PRD API uri : {}, request : {} ", uri, request);
        try {
            ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(uri, request,
                    PaymentResponse.class);
            log.info("Payment response: {} ", responseEntity);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.info("Payment error, exception : {} ", ex);
            return mapper.readValue(ex.getResponseBodyAsString(), PaymentResponse.class);
        }
    }

    private static PaymentRequest buildPaymentRequest(CaseData caseData) {
        long amountToPay = Long.valueOf(caseData.getAmountToPay());
        FeeRequest fee = FeeRequest.builder()
                .calculatedAmount(amountToPay)
                .code(caseData.getFeeCode())
                .version(caseData.getFeeVersion())
                .build();
        return PaymentRequest.builder()
                .accountNumber(caseData.getPbaNumber())
                .caseReference(caseData.getDivorceCaseNumber())
                .customerReference(caseData.getSolicitorReference())
                .organisationNname(caseData.getSolicitorFirm())
                .amount(amountToPay)
                .feesList(Collections.singletonList(fee))
                .build();
    }


    private HttpEntity<PaymentRequest> buildPaymentRequest(String authToken, PaymentRequest paymentRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(paymentRequest, headers);
    }


    private URI buildUri() {
        return UriComponentsBuilder.fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi()).build().toUri();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Inside main");

        CaseData caseData = CaseData.builder()
                .pbaNumber("PBA0072626")
                .divorceCaseNumber("Divorce1")
                .solicitorReference("Sol1")
                .solicitorFirm("Sol Firm")
                .feeCode("FEE0640")
                .feeDescription("finrem")
                .amountToPay("9373")
                .feeVersion("v1")
                .build();

        PaymentRequest pbaAccount = buildPaymentRequest(caseData);
        System.out.println("pbaAccount  = " + pbaAccount);


        JsonNode jsonNode = mapper.convertValue(pbaAccount, JsonNode.class);
        System.out.println("jsonNode  = " + jsonNode);

        String json = "{\"account_number\":\"PBA0072626\",\"case_reference\":\"Divorce1\",\"ccd_case_number\":null,"
                + "\"customer_reference\":\"Sol1\",\"organisation_name\":\"Sol Firm\",\"amount\":9373,"
                + "\"currency\":\"GBP\",\"service\":\"FINREM\",\"site_id\":\"CTSC\","
                + "\"fees\":[{\"calculated_amount\":9373,\"code\":\"FEE0640\",\"version\":\"v1\",\"volume\":1}]}\n";

        PaymentRequest object = mapper.readValue(json, PaymentRequest.class);
        System.out.println("object  = " + object);

    }
}
