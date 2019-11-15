package uk.gov.hmcts.reform.finrem.caseorchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "finrem-payment-client", url = "${payment.api.baseurl}")
public interface PaymentClient {

    @GetMapping(path = "/payments/fee-lookup", produces = APPLICATION_JSON)
    FeeResponse feeLookup(@RequestParam("application-type") String applicationType);


    @GetMapping(path = "/payments/pba-validate/{pbaNumber}", produces = APPLICATION_JSON)
    PBAValidationResponse pbaValidate(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
                                      @PathVariable("pbaNumber") String pbaNumber);


    @PostMapping(path = "/payments/pba-payment",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON,
            produces = APPLICATION_JSON
    )
    PaymentResponse pbaPayment(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
                               @RequestBody PaymentRequest paymentRequest);
}
