package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

public class PaymentByAccountTest {
    private PaymentByAccount pba;

    @Before
    public void setUp() throws Exception {
        String json = "{ \"payment_accounts\" : [\"PBA123\", \"PBA456\"]}";
        ObjectMapper mapper = new ObjectMapper();
        pba = mapper.readValue(json, PaymentByAccount.class);
    }

    @Test
    public void shouldCreatePaymentFromJson() {
        assertThat(pba.getAccountList(), hasItems("PBA123", "PBA456"));
    }
}