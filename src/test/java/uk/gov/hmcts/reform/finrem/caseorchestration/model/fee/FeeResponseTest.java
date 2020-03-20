package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_FEE_CODE;

public class FeeResponseTest {

    private FeeResponse feeResponse;

    @Before
    public void setUp() throws Exception {
        String json = "{ \"code\" : \"FEE0640\", \"fee_amount\" : 50, \"description\" : \"finrem\", "
                +  "\"version\" : \"v1\" }";
        ObjectMapper mapper = new ObjectMapper();
        feeResponse = mapper.readValue(json, FeeResponse.class);
    }

    @Test
    public void shouldCreateFeeResponseFromJson() {
        assertThat(feeResponse.getCode(), is(TEST_FEE_CODE));
        assertThat(feeResponse.getDescription(), is("finrem"));
        assertThat(feeResponse.getVersion(), is("v1"));
        assertThat(feeResponse.getFeeAmount(), is(BigDecimal.valueOf(50)));
    }
}