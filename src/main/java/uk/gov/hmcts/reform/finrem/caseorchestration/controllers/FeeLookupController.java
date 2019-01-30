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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.CCDFeeCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;

import java.math.BigDecimal;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class FeeLookupController extends AbstractBaseController {

    private final FeeService feeService;

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/fee-lookup", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<CCDFeeCallbackResponse> feeLookup(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("Received request for FEE lookup. Auth token: {}, Case request : {}", authToken, ccdRequest);

        validateCaseData(ccdRequest);

        CaseData caseRequestData = ccdRequest.getCaseDetails().getCaseData();
        FeeCaseData feeResponseData = FeeCaseData.builder().build();
        FeeResponse feeResponse = feeService.getApplicationFee();
        updateCaseWithFee(caseRequestData, feeResponseData, feeResponse);
        return ResponseEntity.ok(CCDFeeCallbackResponse.builder().data(feeResponseData).build());
    }

    private void updateCaseWithFee(CaseData caseRequestData, FeeCaseData feeResponseData, FeeResponse feeResponse) {
        FeeItem feeItem = createFeeItem(feeResponse);
        OrderSummary orderSummary = createOrderSummary(caseRequestData, feeItem);
        feeResponseData.setOrderSummary(orderSummary);
        feeResponseData.setAmountToPay(feeItem.getValue().getFeeAmount());
    }

    private OrderSummary createOrderSummary(CaseData caseRequestData, FeeItem feeItem) {
        OrderSummary orderSummary = OrderSummary.builder()
                .paymentTotal(feeItem.getValue().getFeeAmount())
                .paymentReference(caseRequestData.getPbaReference())
                .fees(ImmutableList.of(feeItem))
                .build();
        return orderSummary;
    }

    private FeeItem createFeeItem(FeeResponse feeResponse) {
        FeeValue feeValue = createFeeValue(feeResponse);
        FeeItem feeItem = FeeItem.builder()
                .value(feeValue)
                .build();
        return feeItem;
    }

    private FeeValue createFeeValue(FeeResponse feeResponse) {
        String amountToPay = String.valueOf(feeResponse.getFeeAmount().multiply(BigDecimal.valueOf(100)).longValue());
        FeeValue feeValue = FeeValue.builder()
                .feeCode(feeResponse.getCode())
                .feeAmount(amountToPay)
                .feeVersion(feeResponse.getVersion())
                .feeDescription(feeResponse.getDescription())
                .build();
        return feeValue;
    }

}
