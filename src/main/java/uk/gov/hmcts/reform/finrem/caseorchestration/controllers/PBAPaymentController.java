package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.CCDFeeCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;

import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ConsentedStatus.AWAITING_HWF_DECISION;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class PBAPaymentController implements BaseController {
    private final FeeService feeService;
    private final PBAPaymentService pbaPaymentService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/pba-payment", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<CCDCallbackResponse> pbaPayment(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) throws IOException {
        log.info("Received request for PBA payment. Auth token: {}, Case request : {}", authToken, ccdRequest);

        validateCaseData(ccdRequest);

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        feeLookup(authToken, ccdRequest, caseData);
        if (isPBAPayment(caseData)) {
            if (isPBAPaymentReferenceDoesNotExists(caseData)) {
                String ccdCaseId = ccdRequest.getCaseDetails().getCaseId();
                PaymentResponse paymentResponse = pbaPaymentService.makePayment(authToken, ccdCaseId, caseData);
                if (!paymentResponse.isPaymentSuccess()) {
                    return paymentFailure(caseData, paymentResponse);
                }
                caseData.setState(APPLICATION_SUBMITTED.toString());
                caseData.setPbaPaymentReference(paymentResponse.getReference());
                log.info("Payment succeeded.");
            } else {
                log.info("PBA Payment Reference already exists.");
            }
        } else {
            caseData.setState(AWAITING_HWF_DECISION.toString());
        }
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build());

    }

    private ResponseEntity<CCDCallbackResponse> paymentFailure(CaseData caseData, PaymentResponse paymentResponse) {
        String paymentError = paymentResponse.getPaymentError();
        log.info("Payment by PBA number {} failed, payment error : {} ", caseData.getPbaNumber(),
                paymentResponse.getPaymentError());
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .errors(ImmutableList.of(paymentError))
                .build());
    }

    private void feeLookup(@RequestHeader(value = "Authorization", required = false) String authToken,
                           @RequestBody CCDRequest ccdRequest, CaseData caseData) {
        ResponseEntity<CCDFeeCallbackResponse> feeResponse = new FeeLookupController(feeService)
                .feeLookup(authToken, ccdRequest);
        caseData.setOrderSummary(feeResponse.getBody().getData().getOrderSummary());
        caseData.setAmountToPay(feeResponse.getBody().getData().getAmountToPay());
    }

}
