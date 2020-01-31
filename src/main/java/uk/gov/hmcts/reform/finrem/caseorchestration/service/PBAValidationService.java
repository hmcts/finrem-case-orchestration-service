package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.PaymentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAValidationResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class PBAValidationService {
    private final PaymentClient paymentClient;

    public boolean isValidPBA(String authToken, String pbaNumber) {
        log.info("Inside isValidPBA, authToken : {}, pbaNumber : {}", authToken, pbaNumber);
        PBAValidationResponse pbaResponse = paymentClient.pbaValidate(authToken, pbaNumber);
        log.info("pbaResponse : {}", pbaResponse);

        return pbaResponse.isPbaNumberValid();
    }
}
