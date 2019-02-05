package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FeeTest {
    private Fee fee;

    @Before
    public void setUp() throws Exception {
        String json = "{ \"code\" : \"FEE0640\", \"fee_amount\" : 50 }";
        ObjectMapper mapper = new ObjectMapper();
        fee = mapper.readValue(json, Fee.class);
    }

    @Test
    public void shouldCreateFeeFromJson() {
        assertThat(fee.getCode(), is("FEE0640"));
        assertThat(fee.getFeeAmount(), is(BigDecimal.valueOf(50)));
    }

}