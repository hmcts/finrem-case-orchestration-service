package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.PaymentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {
    private final PaymentClient paymentClient;

    public FeeResponse getApplicationFee() {
        log.info("Inside getApplicationFee");
        return paymentClient.feeLookup();
    }
}
