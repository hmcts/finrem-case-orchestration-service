package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeValue;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.OrderSummary;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.FeeClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {
    private final FeeClient feeClient;

    public FeeResponse getApplicationFee(ApplicationType applicationType, String typeOfApplication) {
        log.info("Inside getApplicationFee, applicationType = {} typeOfApplication {}", applicationType, typeOfApplication);
        return feeClient.getApplicationFee(applicationType, typeOfApplication);
    }

    /**
     * Gets fee payment data for submitting an application.
     * @param caseDetails the case details
     * @return the fee payment case data
     */
    public FeeCaseData getApplicationFeeCaseData(FinremCaseDetails caseDetails) {
        FeeResponse feeResponse = getFee(caseDetails);
        FeeItem feeItem = createFeeItem(feeResponse);

        OrderSummary orderSummary = createOrderSummary(caseDetails, feeItem);
        String amountToPay = feeItem.getValue().getFeeAmount();

        return FeeCaseData.builder()
            .amountToPay(amountToPay)
            .orderSummary(orderSummary)
            .build();
    }

    private FeeResponse getFee(FinremCaseDetails caseDetails) {
        ApplicationType applicationType = ApplicationType.from(caseDetails.getCaseType());
        ScheduleOneWrapper scheduleOneWrapper = caseDetails.getData().getScheduleOneWrapper();
        String typeOfApplication = Optional.ofNullable(scheduleOneWrapper.getTypeOfApplication())
            .map(Schedule1OrMatrimonialAndCpList::getValue)
            .orElse(null);

        return feeClient.getApplicationFee(applicationType, typeOfApplication);
    }

    private OrderSummary createOrderSummary(FinremCaseDetails caseDetails, FeeItem feeItem) {
        String paymentReference = caseDetails.getData().getPbaPaymentReference();
        return OrderSummary.builder()
            .paymentTotal(feeItem.getValue().getFeeAmount())
            .paymentReference(paymentReference)
            .fees(List.of(feeItem))
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
