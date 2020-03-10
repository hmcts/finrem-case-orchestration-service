package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

public class PBAPaymentServiceTest extends BaseServiceTest {

    @Autowired
    private PBAPaymentService pbaPaymentService;

    private CallbackRequest callbackRequest;

    @ClassRule
    public static WireMockClassRule paymentService = new WireMockClassRule(9001);

    private static final String PBA_PAYMENT_API = "/payments/pba-payment";

    @Before
    public void setupCaseData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        callbackRequest = mapper.readValue(new File(getClass()
                .getResource("/fixtures/pba-payment.json").toURI()), CallbackRequest.class);
    }

    @Test
    public void paymentSuccessful() throws Exception {
        setupCaseData();

        setUpPbaPayment("{"
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
                        + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, "123",
                callbackRequest.getCaseDetails().getData());

        assertThat(paymentResponse.getReference(), is("RC-1545-2396-5857-4110"));
        assertThat(paymentResponse.getStatus(), is("Success"));
        assertThat(paymentResponse.isPaymentSuccess(), is(true));
        assertThat(paymentResponse.getPaymentError(), nullValue());
        assertThat(paymentResponse.getStatusHistories().size(), is(1));
    }

    @Test
    public void invalidFunds() throws Exception {
        setupCaseData();

        setUpPbaPayment("{"
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
                        + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, "123",
                callbackRequest.getCaseDetails().getData());

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

        setUpPbaPayment("{"
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
                        + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, "123",
                callbackRequest.getCaseDetails().getData());

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

        setUpPbaPayment("{"
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
                        + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, "123",
                callbackRequest.getCaseDetails().getData());

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

        setUpPbaPayment("{"
                        + "  \"timestamp\": \"2019-01-09T17:59:20.473+0000\","
                        + "  \"status\": 403,"
                        + "  \"error\": \"Forbidden\","
                        + "  \"message\": \"Access Denied\","
                        + "  \"path\": \"/credit-account-payments\""
                        + "}");

        PaymentResponse paymentResponse = pbaPaymentService.makePayment(AUTH_TOKEN, "123",
                callbackRequest.getCaseDetails().getData());

        assertThat(paymentResponse.getReference(), nullValue());
        assertThat(paymentResponse.getStatus(), is("403"));
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getPaymentError(), is("Access Denied"));
        assertThat(paymentResponse.getStatusHistories(), nullValue());
    }

    private void setUpPbaPayment(String response) {
        paymentService.stubFor(post(urlPathEqualTo(PBA_PAYMENT_API))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(response)));
    }
}