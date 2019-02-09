package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PBAPaymentServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.io.IOException;
import java.net.URI;


@Service
@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class PBAPaymentService {
    private final PBAPaymentServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;
    private static final ObjectMapper mapper = new ObjectMapper();

    public PaymentResponse makePayment(String authToken, String ccdCaseId, CaseData caseData) throws IOException {
        PaymentRequest paymentRequest = buildPaymenRequest(ccdCaseId, caseData);
        HttpEntity<PaymentRequest> request = buildPaymentHttpRequest(authToken, paymentRequest);
        URI uri = buildUri();
        log.info("Inside makePayment, PRD API uri : {}, request : {} ", uri, request);
        try {
            ResponseEntity<PaymentResponse> responseEntity = restTemplate.postForEntity(uri, request,
                    PaymentResponse.class);
            log.info("Payment response: {} ", responseEntity);
            return responseEntity.getBody();
        } catch (HttpClientErrorException ex) {
            log.info("Payment error, exception : {} ", ex);
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return PaymentResponse.builder()
                        .error(ex.getStatusCode().toString())
                        .message(ex.getResponseBodyAsString())
                        .build();
            }
            return mapper.readValue(ex.getResponseBodyAsString(), PaymentResponse.class);
        }
    }

    private PaymentRequest buildPaymenRequest(String ccdCaseId, CaseData caseData) {
        FeeRequest feeRequest = buildFeeRequest(caseData);
        log.info("Fee request : {} ", feeRequest);
        return buildPaymentRequest(ccdCaseId, caseData, feeRequest);
    }


    private FeeRequest buildFeeRequest(CaseData caseData) {
        OrderSummary orderSummary = caseData.getOrderSummary();
        FeeItem feeItem = orderSummary.getFees().get(0);
        FeeValue feeValue = feeItem.getValue();
        return FeeRequest.builder()
                .calculatedAmount(Long.valueOf(feeValue.getFeeAmount()))
                .code(feeValue.getFeeCode())
                .version(feeValue.getFeeVersion())
                .build();
    }

    private PaymentRequest buildPaymentRequest(String ccdCaseId, CaseData caseData, FeeRequest fee) {
        return PaymentRequest.builder()
                .accountNumber(caseData.getPbaNumber())
                .caseReference(caseData.getDivorceCaseNumber())
                .customerReference(caseData.getSolicitorReference())
                .ccdCaseNumber(ccdCaseId)
                .description(serviceConfig.getDescription())
                .organisationName(caseData.getSolicitorFirm())
                .siteId(serviceConfig.getSiteId())
                .amount(fee.getCalculatedAmount())
                .feesList(ImmutableList.of(fee))
                .build();
    }


    private HttpEntity<PaymentRequest> buildPaymentHttpRequest(String authToken, PaymentRequest paymentRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(paymentRequest, headers);
    }


    private URI buildUri() {
        return UriComponentsBuilder.fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi()).build().toUri();
    }

}
