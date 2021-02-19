package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PaymentRequestTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreatePaymentRequest() throws Exception {
        String json = "{"
            + " \"account_number\": \"PBA1\","
            + " \"case_reference\": \"caseRef\","
            + " \"ccd_case_number\": \"123\","
            + " \"customer_reference\": \"custRef\","
            + " \"description\": \"desc\","
            + " \"organisation_name\": \"moj\","
            + " \"amount\": 1000,"
            + " \"currency\": \"GBP\","
            + " \"service\": \"FINREM\","
            + " \"case_type\": \"FinancialRemedyMVP2\","
            + " \"fees\": ["
            + "   {"
            + " \"calculated_amount\": 1000,"
            + " \"code\": \"Fee1\","
            + " \"version\": \"v1\","
            + " \"volume\": 1"
            + "   }"
            + " ]"
            + "}";
        PaymentRequest paymentRequest = mapper.readValue(json, PaymentRequest.class);
        assertThat(paymentRequest.getAccountNumber(), is("PBA1"));
        assertThat(paymentRequest.getCaseReference(), is("caseRef"));
        assertThat(paymentRequest.getCcdCaseNumber(), is("123"));
        assertThat(paymentRequest.getCustomerReference(), is("custRef"));
        assertThat(paymentRequest.getDescription(), is("desc"));
        assertThat(paymentRequest.getOrganisationName(), is("moj"));
        assertThat(paymentRequest.getAmount(), is(BigDecimal.valueOf(1000)));
        assertThat(paymentRequest.getCurrency(), is("GBP"));
        assertThat(paymentRequest.getService(), is("FINREM"));
        assertThat(paymentRequest.getCaseType(), is("FinancialRemedyMVP2"));
        assertThat(paymentRequest.getFeesList().size(), is(1));
        assertThat(paymentRequest.getFeesList().get(0).getCalculatedAmount(), is(BigDecimal.valueOf(1000)));
        assertThat(paymentRequest.getFeesList().get(0).getCode(), is("Fee1"));
        assertThat(paymentRequest.getFeesList().get(0).getVersion(), is("v1"));
        assertThat(paymentRequest.getFeesList().get(0).getVolume(), is(1));
    }
}