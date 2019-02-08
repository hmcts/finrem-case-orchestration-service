package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OrderSummaryTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreateOrderSummary() throws Exception {
        String json = "{ "
                    + " \"PaymentReference\": \"Ref1\","
                    + " \"PaymentTotal\": \"1000\","
                    + " \"Fees\": ["
                            + "{ "
                            + " \"value\": {"
                            + " \"FeeDescription\": \"desc\","
                            + " \"FeeVersion\": \"v1\","
                            + " \"FeeCode\": \"code1\","
                            + " \"FeeAmount\": \"1000\""
                            + "}"
                         + "}"
                        + "]"
                    + "}";

        OrderSummary orderSummary = mapper.readValue(json, OrderSummary.class);
        assertThat(orderSummary.getPaymentReference(), is("Ref1"));
        assertThat(orderSummary.getPaymentTotal(), is("1000"));
        assertThat(orderSummary.getFees().size(), is(1));
        assertThat(orderSummary.getFees().get(0).getValue().getFeeDescription(), is("desc"));
        assertThat(orderSummary.getFees().get(0).getValue().getFeeVersion(), is("v1"));
        assertThat(orderSummary.getFees().get(0).getValue().getFeeCode(), is("code1"));
        assertThat(orderSummary.getFees().get(0).getValue().getFeeAmount(), is("1000"));
    }
}