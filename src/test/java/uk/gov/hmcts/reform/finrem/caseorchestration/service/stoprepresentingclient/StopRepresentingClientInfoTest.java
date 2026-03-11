package uk.gov.hmcts.reform.finrem.caseorchestration.service.stoprepresentingclient;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;

class StopRepresentingClientInfoTest {

    @Test
    void shouldReturnFinremCaseData() {
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);

        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
            .caseDetails(caseDetails)
            .build();

        assertThat(info.getFinremCaseData()).isEqualTo(caseData);
    }

    @Test
    void shouldReturnNullWhenCaseDetailsIsNull() {
        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder().build();

        assertThat(info.getFinremCaseData()).isNull();
    }

    @Test
    void shouldReturnFinremCaseDataBefore() {
        FinremCaseDetails caseDetailsBefore = mock(FinremCaseDetails.class);
        FinremCaseData caseDataBefore = mock(FinremCaseData.class);

        when(caseDetailsBefore.getData()).thenReturn(caseDataBefore);

        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        assertThat(info.getFinremCaseDataBefore()).isEqualTo(caseDataBefore);
    }

    @Test
    void shouldReturnCaseId() {
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCaseData caseData = mock(FinremCaseData.class);

        when(caseDetails.getData()).thenReturn(caseData);
        when(caseData.getCcdCaseId()).thenReturn(CASE_ID);

        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder()
            .caseDetails(caseDetails)
            .build();

        assertThat(info.getCaseId()).isEqualTo(CASE_ID_IN_LONG);
    }

    @Test
    void shouldReturnNullCaseIdWhenCaseDataIsNull() {
        StopRepresentingClientInfo info = StopRepresentingClientInfo.builder().build();

        assertThat(info.getCaseId()).isNull();
    }
}
