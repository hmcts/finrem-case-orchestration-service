package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class PBAPaymentServiceTest extends BaseServiceTest {

    @Autowired
    private PBAPaymentService pbaPaymentService;

    protected CCDRequest ccdRequest;

    @Before
    public void setupCaseData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ccdRequest = mapper.readValue(new File(getClass()
                .getResource("/fixtures/pba-payment.json").toURI()), CCDRequest.class);
    }

    @Test
    public void paymentSuccessful() throws Exception {
        setupCaseData();

        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{"
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
                        + "}", MediaType.APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentService.makePayment("token", "123",
                ccdRequest.getCaseDetails().getCaseData());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Success"));
        assertThat(paymentResponse.isPaymentSuccess(), is(true));
        assertThat(paymentResponse.getPaymentError(), nullValue());
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
    }
    
    @Test
    public void invalidFunds() throws Exception {
        setupCaseData();

        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{"
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
                        + "}", MediaType.APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentService.makePayment("token", "123",
                ccdRequest.getCaseDetails().getCaseData());
        
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
    public void accountOnHold() throws Exception {
        setupCaseData();

        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{"
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
                        + "}", MediaType.APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentService.makePayment("token", "123",
                ccdRequest.getCaseDetails().getCaseData());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is on hold"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0003"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is on hold"));
    }


    @Test
    public void accountDeleted() throws Exception {
        setupCaseData();

        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{"
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
                        + "}", MediaType.APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentService.makePayment("token", "123",
                ccdRequest.getCaseDetails().getCaseData());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Failed"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Your account is deleted"));
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorCode(), is("CA-E0004"));
        assertThat(paymentResponse.getStatusHistories().get(0).getErrorMessage(), is("Your account is deleted"));
    }


    @Test
    public void accessIsDenied() throws Exception {
        setupCaseData();

        mockServer.expect(requestTo(toUri()))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{"
                        + "  \"timestamp\": \"2019-01-09T17:59:20.473+0000\","
                        + "  \"status\": 403,"
                        + "  \"error\": \"Forbidden\","
                        + "  \"message\": \"Access Denied\","
                        + "  \"path\": \"/credit-account-payments\""
                        + "}", MediaType.APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentService.makePayment("token", "123",
                ccdRequest.getCaseDetails().getCaseData());

        assertThat(paymentResponse.getReference(), nullValue());
        assertThat(paymentResponse.getStatus(), is("403"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Access Denied"));
        assertThat(paymentResponse.getStatusHistories(), nullValue());
    }


    private String toUri() {
        return "http://test/credit-account-payments";
    }
}