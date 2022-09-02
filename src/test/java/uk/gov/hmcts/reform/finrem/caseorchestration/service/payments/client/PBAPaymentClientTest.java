package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.PaymentsBaseServiceTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.InvalidTokenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.PaymentException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentRequest;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentRequestWithCaseType;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponse;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseErrorToString;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.SetUpUtils.paymentResponseToString;

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

    @Test(expected = PaymentException.class)
    public void makePaymentReceivesClientError() {
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withUnauthorizedRequest()
                .body(paymentResponseErrorToString()).contentType(APPLICATION_JSON));

        pbaPaymentClient.makePaymentWithSiteId(AUTH_TOKEN, paymentRequest());
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

    @Test(expected = PaymentException.class)
    public void makePaymentWithCaseTypeReceivesClientError() {
        mockServer.expect(requestTo(URI))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withUnauthorizedRequest()
                .body(paymentResponseErrorToString()).contentType(APPLICATION_JSON));

        pbaPaymentClient.makePaymentWithCaseType(AUTH_TOKEN, paymentRequestWithCaseType());
    }

    @Test(expected = InvalidTokenException.class)
    public void invalidUserTokenWithCaseType() {
        pbaPaymentClient.makePaymentWithCaseType(INVALID_AUTH_TOKEN, paymentRequestWithCaseType());
    }
}
