package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.finrem.caseorchestration.controllers.BaseController.isConsentedApplication;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_REFERENCE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class FeeLookupController implements BaseController {

    private final FeeService feeService;

    @PostMapping(path = "/fee-lookup", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> feeLookup(
            @RequestHeader(value = "Authorization", required = false) String authToken,
            @RequestBody CallbackRequest callbackRequest) {
        log.info("Received request for FEE lookup. Auth token: {}, Case request : {}", authToken, callbackRequest);

        validateCaseData(callbackRequest);

        Map<String, Object> mapOfCaseData = callbackRequest.getCaseDetails().getData();

        ApplicationType applicationType = isConsentedApplication(mapOfCaseData) ? CONSENTED : CONTESTED;

        FeeResponse feeResponse = feeService.getApplicationFee(applicationType);

        FeeCaseData feeResponseData = FeeCaseData.builder().build();
        updateCaseWithFee(mapOfCaseData, feeResponseData, feeResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
                .data(objectMapper.convertValue(feeResponseData, Map.class)).build());
    }

    private void updateCaseWithFee(Map<String, Object> caseRequestData, FeeCaseData feeResponseData,
                                   FeeResponse feeResponse) {
        FeeItem feeItem = createFeeItem(feeResponse);
        OrderSummary orderSummary = createOrderSummary(caseRequestData, feeItem);
        feeResponseData.setOrderSummary(orderSummary);
        feeResponseData.setAmountToPay(feeItem.getValue().getFeeAmount());
    }

    private OrderSummary createOrderSummary(Map<String, Object> caseRequestData, FeeItem feeItem) {
        return OrderSummary.builder()
                .paymentTotal(feeItem.getValue().getFeeAmount())
                .paymentReference(Objects.toString(caseRequestData.get(PBA_REFERENCE)))
                .fees(ImmutableList.of(feeItem))
                .build();
    }

    private FeeItem createFeeItem(FeeResponse feeResponse) {
        FeeValue feeValue = createFeeValue(feeResponse);
        return FeeItem.builder()
                .value(feeValue)
                .build();
    }

    private FeeValue createFeeValue(FeeResponse feeResponse) {
        String amountToPay = Objects.toString(feeResponse
                .getFeeAmount().multiply(BigDecimal.valueOf(100)).longValue());
        return FeeValue.builder()
                .feeCode(feeResponse.getCode())
                .feeAmount(amountToPay)
                .feeVersion(feeResponse.getVersion())
                .feeDescription(feeResponse.getDescription())
                .build();
    }

}
