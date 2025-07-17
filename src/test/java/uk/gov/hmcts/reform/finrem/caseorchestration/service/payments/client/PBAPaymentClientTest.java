package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.PaymentsBaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.InvalidTokenException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentRequest;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentRequestWithCaseType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponse;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseErrorToString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseToString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAPaymentClient.CLIENT_PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAPaymentClient.SERVER_PAYMENT_ERROR_MESSAGE;

public class PBAPaymentClientTest extends PaymentsBaseServiceTest {

    public static final String AUTH_TOKEN = "Bearer HBJHBKJiuui7097HJH";
    private static final String INVALID_AUTH_TOKEN = "HBJHBKJiuui7097HJH";
    public static final String URI = "http://localhost:8181/credit-account-payments";

    @Autowired
    private PBAPaymentClient pbaPaymentClient;

    @Test
    public void makePayment() {
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(paymentResponseToString(), APPLICATION_JSON));

        PaymentResponse response = pbaPaymentClient.makePaymentWithSiteId(AUTH_TOKEN, paymentRequest());
        assertThat(response, is(paymentResponse()));
    }

    @Test
    public void makePaymentReceivesClientError() {
        String responseBody = paymentResponseErrorToString();
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withUnauthorizedRequest()
                .body(responseBody).contentType(APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentClient.makePaymentWithSiteId(AUTH_TOKEN, paymentRequest());
        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getError(), is(responseBody));
        assertThat(paymentResponse.getMessage(), is(CLIENT_PAYMENT_ERROR_MESSAGE));
    }

    @Test(expected = InvalidTokenException.class)
    public void invalidUserToken() {
        pbaPaymentClient.makePaymentWithSiteId(INVALID_AUTH_TOKEN, paymentRequest());
    }

    @Test
    public void makePaymentWithCaseType() {
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(paymentResponseToString(), APPLICATION_JSON));

        PaymentResponse response = pbaPaymentClient.makePaymentWithCaseType(AUTH_TOKEN, paymentRequestWithCaseType());
        assertThat(response, is(paymentResponse()));
    }

    @Test
    public void makePaymentWithCaseTypeReceivesClientError() {
        String responseBody = paymentResponseErrorToString();
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withUnauthorizedRequest()
                .body(responseBody).contentType(APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentClient.makePaymentWithCaseType(AUTH_TOKEN, paymentRequestWithCaseType());

        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getError(), is(responseBody));
        assertThat(paymentResponse.getMessage(), is(CLIENT_PAYMENT_ERROR_MESSAGE));
    }

    @Test
    public void makePaymentWithCaseTypeReceivesDuplicatePaymentError() {
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withBadRequest()
                .body("duplicate payment").contentType(APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentClient.makePaymentWithCaseType(AUTH_TOKEN, paymentRequestWithCaseType());

        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.isDuplicatePayment(), is(true));
        assertThat(paymentResponse.getError(), is(PaymentResponse.DUPLICATE_PAYMENT_MESSAGE));
        assertThat(paymentResponse.getMessage(), is(CLIENT_PAYMENT_ERROR_MESSAGE));
    }

    @Test(expected = InvalidTokenException.class)
    public void invalidUserTokenWithCaseType() {
        pbaPaymentClient.makePaymentWithCaseType(INVALID_AUTH_TOKEN, paymentRequestWithCaseType());
    }

    @Test
    public void makePaymentWithCaseTypeReceivesServerError() {
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError()
                .body(paymentResponseErrorToString()).contentType(APPLICATION_JSON));

        PaymentResponse paymentResponse = pbaPaymentClient.makePaymentWithCaseType(AUTH_TOKEN, paymentRequestWithCaseType());

        assertThat(paymentResponse.isPaymentSuccess(), is(false));
        assertThat(paymentResponse.getError(), startsWith("500 Internal Server Error"));
        assertThat(paymentResponse.getMessage(), is(SERVER_PAYMENT_ERROR_MESSAGE));
    }
}
