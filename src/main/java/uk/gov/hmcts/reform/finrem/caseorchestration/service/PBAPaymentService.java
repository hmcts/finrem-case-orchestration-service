package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.PaymentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
public class PBAPaymentService {
    private final PaymentClient paymentClient;

    @Value("${payment.api.siteId}")
    private String siteId;

    @Value("${payment.api.description}")
    private String description;


    public PaymentResponse makePayment(String authToken, String ccdCaseId, CaseData caseData) {
        log.info("Inside makePayment, authToken : {}, ccdCaseId : {}, caseData : {}", authToken, ccdCaseId, caseData);
        PaymentRequest paymentRequest = buildPaymentRequest(ccdCaseId, caseData);
        log.info("paymentRequest: {}", paymentRequest);
        PaymentResponse paymentResponse = paymentClient.pbaPayment(authToken, paymentRequest);
        log.info("paymentResponse : {} ", paymentResponse);
        return paymentResponse;
    }

    private PaymentRequest buildPaymentRequest(String ccdCaseId, CaseData caseData) {
        FeeRequest fee = buildFeeRequest(caseData);
        log.info("Fee request : {} ", fee);
        return PaymentRequest.builder()
                .accountNumber(caseData.getPbaNumber())
                .caseReference(caseData.getDivorceCaseNumber())
                .customerReference(caseData.getPbaReference())
                .ccdCaseNumber(ccdCaseId)
                .description(description)
                .organisationName(caseData.getSolicitorFirm())
                .siteId(siteId)
                .amount(fee.getCalculatedAmount())
                .feesList(ImmutableList.of(fee))
                .build();
    }

    private FeeRequest buildFeeRequest(CaseData caseData) {
        OrderSummary orderSummary = caseData.getOrderSummary();
        FeeItem feeItem = orderSummary.getFees().get(0);
        FeeValue feeValue = feeItem.getValue();
        BigDecimal feeAmount = new BigDecimal(feeValue.getFeeAmount()).divide(BigDecimal.valueOf(100));
        return FeeRequest.builder()
                .calculatedAmount(feeAmount)
                .code(feeValue.getFeeCode())
                .version(feeValue.getFeeVersion())
                .build();
    }
}
