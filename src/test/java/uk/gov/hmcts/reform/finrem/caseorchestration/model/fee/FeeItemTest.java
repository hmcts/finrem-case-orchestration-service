package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FeeItemTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreateFeeItem() throws Exception {
        String json = "{ "
                + " \"value\": {"
                    + " \"FeeDescription\": \"desc\","
                    + " \"FeeVersion\": \"v1\","
                    + " \"FeeCode\": \"code1\","
                    + " \"FeeAmount\": \"1000\""
                    + "}"
                + "}";
        FeeItem feeItem = mapper.readValue(json, FeeItem.class);
        assertThat(feeItem.getValue().getFeeDescription(), is("desc"));
        assertThat(feeItem.getValue().getFeeVersion(), is("v1"));
        assertThat(feeItem.getValue().getFeeCode(), is("code1"));
        assertThat(feeItem.getValue().getFeeAmount(), is("1000"));
    }
}