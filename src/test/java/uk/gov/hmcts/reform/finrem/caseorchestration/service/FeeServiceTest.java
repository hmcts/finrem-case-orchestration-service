package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ScheduleOneWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.FeeClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Schedule1OrMatrimonialAndCpList.SCHEDULE_1_CHILDREN_ACT_1989;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    @InjectMocks
    private FeeService feeService;
    @Mock
    private FeeClient feeClient;

    @Test
    void testGetApplicationFee() {
        FeeResponse feeResponse = new FeeResponse();
        when(feeClient.getApplicationFee(CONSENTED, SCHEDULE_1_CHILDREN_ACT_1989.getValue())).thenReturn(feeResponse);
        FeeResponse response = feeService.getApplicationFee(CONSENTED, SCHEDULE_1_CHILDREN_ACT_1989.getValue());
        assertThat(response).isEqualTo(feeResponse);
    }

    @Test
    void givenSchedule1ContestedCase_whenGetApplicationFeeCaseData_thenReturnsFeeCaseData() {
        FinremCaseData caseData = FinremCaseData.builder()
            .pbaPaymentReference("PBA1234567")
            .scheduleOneWrapper(ScheduleOneWrapper.builder()
                .typeOfApplication(SCHEDULE_1_CHILDREN_ACT_1989)
                .build())
            .build();
        FinremCaseDetails caseDetails =
            FinremCaseDetails.builder()
                .caseType(CaseType.CONTESTED)
                .data(caseData)
                .build();

        FeeResponse feeResponse = FeeResponse.builder()
            .code("FEE0229")
            .feeAmount(BigDecimal.valueOf(50.99))
            .description("desc")
            .version("1.0")
            .build();
        when(feeClient.getApplicationFee(CONTESTED, SCHEDULE_1_CHILDREN_ACT_1989.getValue())).thenReturn(feeResponse);

        FeeCaseData feeCaseData = feeService.getApplicationFeeCaseData(caseDetails);
        verifyFeeCaseData(feeCaseData);
    }

    @Test
    void givenConsentedCase_whenGetApplicationFeeCaseData_thenReturnsFeeCaseData() {
        FinremCaseData caseData = FinremCaseData.builder()
            .pbaPaymentReference("PBA1234567")
            .build();
        FinremCaseDetails caseDetails =
            FinremCaseDetails.builder()
                .caseType(CaseType.CONSENTED)
                .data(caseData)
                .build();

        FeeResponse feeResponse = FeeResponse.builder()
            .code("FEE0229")
            .feeAmount(BigDecimal.valueOf(50.99))
            .description("desc")
            .version("1.0")
            .build();
        when(feeClient.getApplicationFee(CONSENTED, null)).thenReturn(feeResponse);

        FeeCaseData feeCaseData = feeService.getApplicationFeeCaseData(caseDetails);
        verifyFeeCaseData(feeCaseData);
    }

    private void verifyFeeCaseData(FeeCaseData feeCaseData) {
        assertThat(feeCaseData.getAmountToPay()).isEqualTo("5099");

        assertThat(feeCaseData.getOrderSummary().getPaymentReference()).isEqualTo("PBA1234567");
        assertThat(feeCaseData.getOrderSummary().getPaymentTotal()).isEqualTo("5099");

        List<FeeItem> feeItems = feeCaseData.getOrderSummary().getFees();
        assertThat(feeItems).hasSize(1);
        assertThat(feeItems.getFirst().getValue().getFeeAmount()).isEqualTo("5099");
        assertThat(feeItems.getFirst().getValue().getFeeCode()).isEqualTo("FEE0229");
        assertThat(feeItems.getFirst().getValue().getFeeDescription()).isEqualTo("desc");
        assertThat(feeItems.getFirst().getValue().getFeeVersion()).isEqualTo("1.0");
    }
}
