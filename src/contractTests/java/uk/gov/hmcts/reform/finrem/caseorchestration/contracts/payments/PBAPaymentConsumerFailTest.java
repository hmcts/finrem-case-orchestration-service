package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequestWithSiteID;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAPaymentClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments.PBAPaymentConsumerSuccessTest.getDslPart;
import static uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments.PBAPaymentConsumerSuccessTest.getPaymentRequest;


@SpringBootTest({"payment.url: http://localhost:8887"})
@TestPropertySource(locations = "classpath:application.properties")
public class PBAPaymentConsumerFailTest extends BaseTest {

    public static final String AUTH_TOKEN = "Bearer someAuthorizationToken";
    public static final BigDecimal ONE_THOUSAND_FIVE_HUNDRED = new BigDecimal("1500.00");

    @Autowired
    PBAPaymentClient pbaPaymentClient;

    @Autowired
    ObjectMapper objectMapper;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("payment_creditAccountPayment", "localhost", 8887, this);


    @Pact(provider = "payment_creditAccountPayment", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentFail(PactDslWithProvider builder) throws IOException {

        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("accountNumber", "test.account");
        paymentMap.put("availableBalance", "1000.00");
        paymentMap.put("accountName", "test.account.name");

        return builder
            .given("An active account has insufficient funds for a payment", paymentMap)
            .uponReceiving("A request for a payment")
            .path("/credit-account-payments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            .body(objectMapper.writeValueAsString(getPaymentRequest(ONE_THOUSAND_FIVE_HUNDRED)))
            .willRespondWith()
            .status(403)
            .body(buildPBAPaymentResponseDsl("Fail", "failed", "CA-E0001", "Insufficient funds available"))
            .toPact();
    }

    @Test
    @PactVerification(fragment = "generatePactFragmentFail")
    public void verifyPBAPaymentPactFail() {
        verifyForbiddenRequest(new BigDecimal("1500.00"));
    }

    private void verifyForbiddenRequest(BigDecimal amount) {
        PaymentRequestWithSiteID paymentRequest = getPaymentRequest(amount);
        assertThrows(Exception.class, () -> {
            pbaPaymentClient.makePaymentWithSiteId(AUTH_TOKEN, paymentRequest);
        });
    }

    private DslPart buildPBAPaymentResponseDsl(String status, String paymentStatus, String errorCode, String errorMessage) {
        return getDslPart(status, paymentStatus, errorCode, errorMessage);
    }
}
