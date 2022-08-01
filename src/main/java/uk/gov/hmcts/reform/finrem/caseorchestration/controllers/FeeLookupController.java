package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeeService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.PBA_REFERENCE;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/case-orchestration")
@Slf4j
@SuppressWarnings("unchecked")
public class FeeLookupController extends BaseController {

    private final FeeService feeService;
    private final CaseDataService caseDataService;

    @PostMapping(path = "/fee-lookup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles looking up Case Fees")
    public ResponseEntity<AboutToStartOrSubmitCallbackResponse> feeLookup(
        @RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authToken,
        @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request for Fee lookup for Case ID {}", caseDetails.getId());

        validateCaseData(callbackRequest);

        ApplicationType applicationType = caseDataService.isConsentedApplication(caseDetails) ? CONSENTED : CONTESTED;
        FeeResponse feeResponse = feeService.getApplicationFee(applicationType);

        FeeCaseData feeResponseData = FeeCaseData.builder().build();
        Map<String, Object> mapOfCaseData = caseDetails.getData();
        updateCaseWithFee(mapOfCaseData, feeResponseData, feeResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        return ResponseEntity.ok(AboutToStartOrSubmitCallbackResponse.builder()
            .data(objectMapper.convertValue(feeResponseData, Map.class)).build());
    }

    private void updateCaseWithFee(Map<String, Object> caseRequestData, FeeCaseData feeResponseData, FeeResponse feeResponse) {
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
