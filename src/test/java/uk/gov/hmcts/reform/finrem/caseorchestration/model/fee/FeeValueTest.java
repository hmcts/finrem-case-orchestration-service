package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FeeValueTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreateFeeValue() throws Exception {
        String json = "{"
                + " \"FeeDescription\": \"desc\","
                + " \"FeeVersion\": \"v1\","
                + " \"FeeCode\": \"code1\","
                + " \"FeeAmount\": \"1000\""
                + "}";
        FeeValue feeValue = mapper.readValue(json, FeeValue.class);
        assertThat(feeValue.getFeeDescription(), is("desc"));
        assertThat(feeValue.getFeeVersion(), is("v1"));
        assertThat(feeValue.getFeeCode(), is("code1"));
        assertThat(feeValue.getFeeAmount(), is("1000"));
    }
}