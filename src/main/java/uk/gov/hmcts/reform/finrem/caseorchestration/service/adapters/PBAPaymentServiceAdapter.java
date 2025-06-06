package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;

@Service
public class PBAPaymentServiceAdapter {
    private final PBAPaymentService pbaPaymentService;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    public PBAPaymentServiceAdapter(PBAPaymentService pbaPaymentService,
                                    FinremCaseDetailsMapper finremCaseDetailsMapper) {
        this.pbaPaymentService = pbaPaymentService;
        this.finremCaseDetailsMapper = finremCaseDetailsMapper;
    }

    public PaymentResponse makePayment(String authToken, FinremCaseDetails caseDetails) {
        // Convert FinremCaseDetails to the required type for PBAPaymentService
        CaseDetails caseDetailsForPBA = finremCaseDetailsMapper.mapToCaseDetails(caseDetails);
        return pbaPaymentService.makePayment(authToken, caseDetailsForPBA);
    }
}
