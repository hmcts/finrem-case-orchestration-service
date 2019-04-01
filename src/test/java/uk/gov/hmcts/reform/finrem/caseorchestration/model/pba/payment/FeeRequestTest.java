package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FeeRequestTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreateFeeRequest() throws Exception {
        String json = "{"
                + " \"calculated_amount\": 1000,"
                + " \"code\": \"Fee1\","
                + " \"version\": \"v1\","
                + " \"volume\": 1"
                + "}";
        FeeRequest feeRequest = mapper.readValue(json, FeeRequest.class);
        assertThat(feeRequest.getCalculatedAmount(), is(BigDecimal.valueOf(1000)));
        assertThat(feeRequest.getCode(), is("Fee1"));
        assertThat(feeRequest.getVersion(), is("v1"));
        assertThat(feeRequest.getVolume(), is(1));
    }
}