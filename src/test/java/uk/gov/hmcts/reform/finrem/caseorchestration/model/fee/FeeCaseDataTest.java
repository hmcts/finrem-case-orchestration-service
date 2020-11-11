package uk.gov.hmcts.reform.finrem.caseorchestration.model.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FeeCaseDataTest {
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreateFeeCaseData() throws Exception {
        String json = "{ "
            + " \"amountToPay\":  \"1000\","
            + " \"orderSummary\": {"
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
            + "}"
            + "}";

        FeeCaseData feeCaseData = mapper.readValue(json, FeeCaseData.class);
        assertThat(feeCaseData.getAmountToPay(), is("1000"));
        assertThat(feeCaseData.getOrderSummary().getPaymentReference(), is("Ref1"));
        assertThat(feeCaseData.getOrderSummary().getPaymentTotal(), is("1000"));
        assertThat(feeCaseData.getOrderSummary().getFees().size(), is(1));
        assertThat(feeCaseData.getOrderSummary().getFees().get(0).getValue().getFeeDescription(), is("desc"));
        assertThat(feeCaseData.getOrderSummary().getFees().get(0).getValue().getFeeVersion(), is("v1"));
        assertThat(feeCaseData.getOrderSummary().getFees().get(0).getValue().getFeeCode(), is("code1"));
        assertThat(feeCaseData.getOrderSummary().getFees().get(0).getValue().getFeeAmount(), is("1000"));
    }
}