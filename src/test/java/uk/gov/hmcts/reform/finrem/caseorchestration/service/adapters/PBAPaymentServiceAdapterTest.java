package uk.gov.hmcts.reform.finrem.caseorchestration.service.adapters;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PBAPaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

class PBAPaymentServiceAdapterTest {

    @Test
    void testMakePayment() {
        PBAPaymentService pbaPaymentService = mock(PBAPaymentService.class);
        FinremCaseDetailsMapper finremCaseDetailsMapper = mock(FinremCaseDetailsMapper.class);

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().build();
        CaseDetails caseDetails = CaseDetails.builder().build();

        when(finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails)).thenReturn(caseDetails);

        PaymentResponse paymentResponse = new PaymentResponse();
        when(pbaPaymentService.makePayment(AUTH_TOKEN, caseDetails)).thenReturn(paymentResponse);

        PBAPaymentServiceAdapter service = new PBAPaymentServiceAdapter(pbaPaymentService, finremCaseDetailsMapper);
        PaymentResponse response = service.makePayment(AUTH_TOKEN, finremCaseDetails);

        assertThat(response).isEqualTo(paymentResponse);
        verify(pbaPaymentService).makePayment(AUTH_TOKEN, caseDetails);
    }
}
