package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;

class FinremCallbackRequestTest {
    
    @Test
    void getFinremCaseData_shouldReturnData_whenCaseDetailsPresent() {
        FinremCaseData expectedData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().data(expectedData).build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetails(caseDetails);

        assertThat(underTest.getFinremCaseData())
            .isSameAs(expectedData);
    }

    @Test
    void getFinremCaseData_shouldReturnNull_whenCaseDetailsIsNull() {
        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();

        assertThat(underTest.getFinremCaseData())
            .isNull();
    }

    @Test
    void getFinremCaseData_shouldReturnNull_whenDataIsNull() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetails(caseDetails);

        assertThat(underTest.getFinremCaseData())
            .isNull();
    }

    @Test
    void getFinremCaseDataBefore_shouldReturnData_whenCaseDetailsBeforePresent() {
        FinremCaseData expectedData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().data(expectedData).build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetailsBefore(caseDetailsBefore);

        assertThat(underTest.getFinremCaseDataBefore())
            .isSameAs(expectedData);
    }

    @Test
    void getFinremCaseDataBefore_shouldReturnNull_whenCaseDetailsBeforeIsNull() {
        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();

        assertThat(underTest.getFinremCaseDataBefore())
            .isNull();
    }

    @Test
    void getFinremCaseDataBefore_shouldReturnNull_whenDataIsNull() {
        FinremCaseDetails caseDetailsBefore = FinremCaseDetails.builder().build();

        FinremCallbackRequest underTest = FinremCallbackRequest.builder().build();
        underTest.setCaseDetailsBefore(caseDetailsBefore);

        assertThat(underTest.getFinremCaseDataBefore())
            .isNull();
    }
}
