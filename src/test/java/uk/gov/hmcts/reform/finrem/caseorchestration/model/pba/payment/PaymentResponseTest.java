package uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class PaymentResponseTest {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldCreatePaymentResponseWhenSuccess() throws Exception {
        String json = "{"
                + " \"reference\": \"RC-1545-2396-5857-4110\","
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + " \"status\": \"Success\","
                + " \"status_histories\": ["
                + "   {"
                + "     \"status\": \"success\","
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
                + "   }"
                + " ]"
                + "}";
        PaymentResponse paymentResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Success"));
        assertThat(paymentResponse.isPaymentSuccess(), is(true));
        assertThat(paymentResponse.getPaymentError(), nullValue());
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
    }


    @Test
    public void shouldCreatePaymentResponseWhenInvalidFunds() throws Exception {
        String json = "{"
                + " \"reference\": \"RC-1545-2396-5857-4110\","
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + " \"status\": \"Failed\","
                + " \"status_histories\": ["
                + "   {"
                + "     \"status\": \"failed\","
                + "     \"error_code\": \"CA-E0001\","
                + "     \"error_message\": \"You have insufficient funds available\","
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
                + "   }"
                + " ]"
                + "}";
        PaymentResponse paymentResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("You have insufficient funds available"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0001"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(),
                is("You have insufficient funds available"));
    }


    @Test
    public void shouldCreatePaymentResponseWhenAccountOnHold() throws Exception {
        String json = "{"
                + " \"reference\": \"RC-1545-2396-5857-4110\","
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + " \"status\": \"Failed\","
                + " \"status_histories\": ["
                + "   {"
                + "     \"status\": \"failed\","
                + "     \"error_code\": \"CA-E0003\","
                + "     \"error_message\": \"Your account is on hold\","
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
                + "   }"
                + " ]"
                + "}";
        PaymentResponse paymentResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is on hold"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0003"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is on hold"));
    }


    @Test
    public void shouldCreatePaymentResponseWhenAccountDeleted() throws Exception {
        String json = "{"
                + " \"reference\": \"RC-1545-2396-5857-4110\","
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + " \"status\": \"Failed\","
                + " \"status_histories\": ["
                + "   {"
                + "     \"status\": \"failed\","
                + "     \"error_code\": \"CA-E0004\","
                + "     \"error_message\": \"Your account is deleted\","
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\","
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\""
                + "   }"
                + " ]"
                + "}";
        PaymentResponse paymentResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is deleted"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0004"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is deleted"));
    }


    @Test
    public void shouldCreatePaymentResponseWhenAccessIsDenied() throws Exception {
        String json = "{"
                + "  \"timestamp\": \"2019-01-09T17:59:20.473+0000\","
                + "  \"status\": 403,"
                + "  \"error\": \"Forbidden\","
                + "  \"message\": \"Access Denied\","
                + "  \"path\": \"/credit-account-payments\""
                + "}";
        PaymentResponse paymentResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(paymentResponse.getReference(), nullValue());
        assertThat(paymentResponse.getStatus(), is("403"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Access Denied"));
        assertThat(paymentResponse.getStatusHistories(), nullValue());
    }

}