package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAAccount;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

public class PBAAccountTest {
    PBAAccount pba;

    @Before
    public void setUp() throws Exception {
        String json = "{ \"payment_accounts\" : [\"PBA123\", \"PBA456\"]}";
        ObjectMapper mapper = new ObjectMapper();
        pba = mapper.readValue(json, PBAAccount.class);
    }

    @Test
    public void shouldCreatePaymentFromJson() {
        assertThat(pba.getAccountList(), hasItems("PBA123", "PBA456"));
    }
}