package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.PaymentClient;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.math.BigDecimal;
import java.util.Map;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CONSENTED_SOLICITOR_FIRM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FEES;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FEE_AMOUNT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FEE_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FEE_VERSION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ORDER_SUMMARY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.CommonFunction.isConsentedApplication;

@Service
@RequiredArgsConstructor
@Slf4j
public class PBAPaymentService {

    private final PaymentClient paymentClient;

    @Value("${payment.api.siteId}")
    private String siteId;

    @Value("${payment.api.consented-description}")
    private String consentedDescription;

    @Value("${payment.api.contested-description}")
    private String contestedDescription;

    public PaymentResponse makePayment(String authToken, CaseDetails caseDetails) {
        log.info("Inside makePayment for ccdCaseId : {}", caseDetails.getId());
        PaymentRequest paymentRequest = buildPaymentRequest(caseDetails);
        log.info("paymentRequest: {}", paymentRequest);
        PaymentResponse paymentResponse = paymentClient.pbaPayment(authToken, paymentRequest);
        log.info("paymentResponse : {} ", paymentResponse);
        return paymentResponse;
    }

    private PaymentRequest buildPaymentRequest(CaseDetails caseDetails) {
        FeeRequest feeRequest = buildFeeRequest(caseDetails.getData());
        log.info("Fee request : {} ", feeRequest);
        return buildPaymentRequestWithFee(caseDetails, feeRequest);
    }

    private FeeRequest buildFeeRequest(Map<String, Object> mapOfCaseData) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataJsonNode = mapper.valueToTree(mapOfCaseData);
        JsonNode feeValueAsJson = dataJsonNode.path(ORDER_SUMMARY).path(FEES).get(0).path(VALUE);
        BigDecimal feeAmount =
            BigDecimal.valueOf(feeValueAsJson.path(FEE_AMOUNT).asDouble()).divide(BigDecimal.valueOf(100));

        return FeeRequest
            .builder()
            .calculatedAmount(feeAmount)
            .code(feeValueAsJson.path(FEE_CODE).asText())
            .version(feeValueAsJson.path(FEE_VERSION).asText())
            .build();
    }

    private PaymentRequest buildPaymentRequestWithFee(CaseDetails caseDetails, FeeRequest fee) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataJsonNode = mapper.valueToTree(caseDetails.getData());
        String ccdCaseId = String.valueOf(caseDetails.getId());
        String description = isConsentedApplication(caseDetails) ? consentedDescription : contestedDescription;
        return PaymentRequest.builder()
            .accountNumber(dataJsonNode.path(PBA_NUMBER).asText())
            .caseReference(dataJsonNode.path(DIVORCE_CASE_NUMBER).asText())
            .customerReference(dataJsonNode.path(PBA_REFERENCE).asText())
            .ccdCaseNumber(ccdCaseId)
            .description(description)
            .organisationName(dataJsonNode.path(CONSENTED_SOLICITOR_FIRM).asText())
            .siteId(siteId)
            .amount(fee.getCalculatedAmount())
            .feesList(ImmutableList.of(fee))
            .build();
    }
}
