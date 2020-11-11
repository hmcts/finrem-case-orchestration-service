package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PaymentStatusHistoryTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreatePaymentStatusHistory() throws Exception {
        String json = "{"
            + " \"status\": \"success\","
            + " \"error_code\": \"err\","
            + " \"error_message\": \"failed\""
            + "}";
        PaymentStatusHistory paymentStatusHistory = mapper.readValue(json, PaymentStatusHistory.class);
        assertThat(paymentStatusHistory.getStatus(), is("success"));
        assertThat(paymentStatusHistory.getErrorCode(), is("err"));
        assertThat(paymentStatusHistory.getErrorMessage(), is("failed"));
    }
}