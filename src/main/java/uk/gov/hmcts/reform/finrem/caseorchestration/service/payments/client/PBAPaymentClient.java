package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequestWithSiteID;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.config.PBAPaymentServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.InvalidTokenException;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class PBAPaymentClient {

    /** Error message to be returned when the payment service encounters a client error such as invalid PBA account. */
    public static final String CLIENT_PAYMENT_ERROR_MESSAGE = "Payment failed. Please review your account.";
    /** Error message to be returned when the payment service encounters an error. */
    public static final String SERVER_PAYMENT_ERROR_MESSAGE = "An error occurred while processing the payment. Please try again later.";

    private final PBAPaymentServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    public PaymentResponse makePaymentWithSiteId(String authToken, PaymentRequestWithSiteID paymentRequest) {
        HttpEntity<PaymentRequestWithSiteID> request = buildPaymentRequestWithSiteId(authToken, paymentRequest);
        URI uri = buildUri();
        log.info("Inside makePayment, payment API uri : {}, request : {} ", uri, request);
        try {
            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(uri, request, PaymentResponse.class);
            log.info("Payment response: {} ", response);
            return response.getBody();
        } catch (Exception ex) {
            log.info("Payment with SiteId failed with exception : {}", ex.getMessage());
            return handlePaymentException(ex);
        }
    }

    private HttpEntity<PaymentRequestWithSiteID> buildPaymentRequestWithSiteId(String authToken, PaymentRequestWithSiteID paymentRequest) {
        HttpHeaders headers = new HttpHeaders();
        if (!authToken.matches("^Bearer .+")) {
            throw new InvalidTokenException("Invalid user token");
        }
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(paymentRequest, headers);
    }

    public PaymentResponse makePaymentWithCaseType(String authToken, PaymentRequest paymentRequest) {
        HttpEntity<PaymentRequest> request = buildPaymentRequestWithCaseType(authToken, paymentRequest);
        URI uri = buildUri();
        log.info("Inside makePayment, payment API uri : {}, request : {} ", uri, request);
        try {
            ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(uri, request, PaymentResponse.class);
            log.info("Payment response: {} ", response);
            return response.getBody();
        } catch (Exception ex) {
            log.info("Payment with CaseType failed with exception : {}", ex.getMessage());
            return handlePaymentException(ex);
        }
    }

    private HttpEntity<PaymentRequest> buildPaymentRequestWithCaseType(String authToken, PaymentRequest paymentRequest) {
        HttpHeaders headers = new HttpHeaders();
        if (!authToken.matches("^Bearer .+")) {
            throw new InvalidTokenException("Invalid user token");
        }
        headers.add("Authorization", authToken);
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(paymentRequest, headers);
    }

    private URI buildUri() {
        return UriComponentsBuilder.fromUriString(serviceConfig.getUrl() + serviceConfig.getApi()).build().toUri();
    }

    private PaymentResponse handlePaymentException(Exception e) {
        // The payments API returns HTTP 4xx errors when there is a user error with the PBA account
        if (e instanceof HttpClientErrorException httpClientErrorException) {
            return get400ErrorResponse(httpClientErrorException);
        } else {
            return PaymentResponse.builder()
                .error(e.getMessage())
                .message(SERVER_PAYMENT_ERROR_MESSAGE)
                .build();
        }
    }

    private PaymentResponse get400ErrorResponse(HttpClientErrorException e) {
        return PaymentResponse.builder()
            .error(e.getResponseBodyAsString())
            .message(CLIENT_PAYMENT_ERROR_MESSAGE)
            .build();
    }
}
