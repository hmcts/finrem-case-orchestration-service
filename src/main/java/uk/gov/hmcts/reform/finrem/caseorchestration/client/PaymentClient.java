package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAValidationResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@FeignClient(name = "finrem-payment-client", url = "${payment.api.baseurl}")
public interface PaymentClient {

    @GetMapping(path = "/payments/fee-lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    FeeResponse feeLookup(@RequestParam("application-type") String applicationType);


    @GetMapping(path = "/payments/pba-validate/{pbaNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    PBAValidationResponse pbaValidate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
                                      @PathVariable("pbaNumber") String pbaNumber);


    @PostMapping(path = "/payments/pba-payment",
            headers = CONTENT_TYPE + "=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    PaymentResponse pbaPayment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
                               @RequestBody PaymentRequest paymentRequest);
}
