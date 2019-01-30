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
        String json = "{\n"
                + " \"reference\": \"RC-1545-2396-5857-4110\",\n"
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + " \"status\": \"Success\",\n"
                + " \"status_histories\": [\n"
                + "   {\n"
                + "     \"status\": \"success\",\n"
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\"\n"
                + "   }\n"
                + " ]\n"
                + "}";
        PaymentResponse pbaResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(pbaResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(pbaResponse.getStatus(), is("Success"));
        assertThat(pbaResponse.isPaymentSuccess(), is(true));
        assertThat(pbaResponse.getPaymentError(), nullValue());
        assertThat(pbaResponse.getStatusHistories().size(), is(1));
    }


    @Test
    public void shouldCreatePaymentResponseWhenInvalidFunds() throws Exception {
        String json = "{\n"
                + " \"reference\": \"RC-1545-2396-5857-4110\",\n"
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + " \"status\": \"Failed\",\n"
                + " \"status_histories\": [\n"
                + "   {\n"
                + "     \"status\": \"failed\",\n"
                + "     \"error_code\": \"CA-E0001\",\n"
                + "     \"error_message\": \"You have insufficient funds available\",\n"
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\"\n"
                + "   }\n"
                + " ]\n"
                + "}";
        PaymentResponse pbaResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(pbaResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(pbaResponse.getStatus(), is("Failed"));
        assertThat(pbaResponse.isPaymentSuccess(), is(false));
        assertThat(pbaResponse.getPaymentError(), is("You have insufficient funds available"));
        assertThat(pbaResponse.getStatusHistories().size(), is(1));
        assertThat(pbaResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0001"));
        assertThat(pbaResponse.getStatusHistories().get(0).getErrorMessage(),
                is("You have insufficient funds available"));
    }


    @Test
    public void shouldCreatePaymentResponseWhenAccountOnHold() throws Exception {
        String json = "{\n"
                + " \"reference\": \"RC-1545-2396-5857-4110\",\n"
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + " \"status\": \"Failed\",\n"
                + " \"status_histories\": [\n"
                + "   {\n"
                + "     \"status\": \"failed\",\n"
                + "     \"error_code\": \"CA-E0003\",\n"
                + "     \"error_message\": \"Your account is on hold\",\n"
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\"\n"
                + "   }\n"
                + " ]\n"
                + "}";
        PaymentResponse pbaResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(pbaResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(pbaResponse.getStatus(), is("Failed"));
        assertThat(pbaResponse.isPaymentSuccess(), is(false));
        assertThat(pbaResponse.getPaymentError(), is("Your account is on hold"));
        assertThat(pbaResponse.getStatusHistories().size(), is(1));
        assertThat(pbaResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0003"));
        assertThat(pbaResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is on hold"));
    }


    @Test
    public void shouldCreatePaymentResponseWhenAccountDeleted() throws Exception {
        String json = "{\n"
                + " \"reference\": \"RC-1545-2396-5857-4110\",\n"
                + " \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + " \"status\": \"Failed\",\n"
                + " \"status_histories\": [\n"
                + "   {\n"
                + "     \"status\": \"failed\",\n"
                + "     \"error_code\": \"CA-E0004\",\n"
                + "     \"error_message\": \"Your account is deleted\",\n"
                + "     \"date_created\": \"2018-12-19T17:14:18.572+0000\",\n"
                + "     \"date_updated\": \"2018-12-19T17:14:18.572+0000\"\n"
                + "   }\n"
                + " ]\n"
                + "}";
        PaymentResponse pbaResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(pbaResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(pbaResponse.getStatus(), is("Failed"));
        assertThat(pbaResponse.isPaymentSuccess(), is(false));
        assertThat(pbaResponse.getPaymentError(), is("Your account is deleted"));
        assertThat(pbaResponse.getStatusHistories().size(), is(1));
        assertThat(pbaResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0004"));
        assertThat(pbaResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is deleted"));
    }


    @Test
    public void shouldCreatePaymentResponseWhenAccessIsDenied() throws Exception {
        String json = "{\n"
                + "  \"timestamp\": \"2019-01-09T17:59:20.473+0000\",\n"
                + "  \"status\": 403,\n"
                + "  \"error\": \"Forbidden\",\n"
                + "  \"message\": \"Access Denied\",\n"
                + "  \"path\": \"/credit-account-payments\"\n"
                + "}";
        PaymentResponse pbaResponse = mapper.readValue(json, PaymentResponse.class);
        assertThat(pbaResponse.getReference(), nullValue());
        assertThat(pbaResponse.getStatus(), is("403"));
        assertThat(pbaResponse.isPaymentSuccess(), is(false));
        assertThat(pbaResponse.getPaymentError(), is("Access Denied"));
        assertThat(pbaResponse.getStatusHistories(), nullValue());
    }

}