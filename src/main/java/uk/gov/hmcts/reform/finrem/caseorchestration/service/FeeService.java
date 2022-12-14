package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.FeeClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {
    private final FeeClient feeClient;

    public FeeResponse getApplicationFee(ApplicationType applicationType, String typeOfApplication) {
        log.info("Inside getApplicationFee, applicationType = {} typeOfApplication {}", applicationType, typeOfApplication);
        return feeClient.getApplicationFee(applicationType, typeOfApplication);
    }
}
