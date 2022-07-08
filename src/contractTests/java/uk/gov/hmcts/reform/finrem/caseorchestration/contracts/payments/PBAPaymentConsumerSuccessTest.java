package uk.gov.hmcts.reform.finrem.caseorchestration.contracts.payments;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.FeeRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentRequestWithSiteID;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.payment.PaymentResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client.PBAPaymentClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@SpringBootTest({"payment.url: http://localhost:8886"})
@TestPropertySource(locations = "classpath:application.properties")
@PactFolder("pacts")
public class PBAPaymentConsumerSuccessTest extends BaseTest {

    public static final String AUTH_TOKEN = "Bearer someAuthorizationToken";

    @Autowired
    PBAPaymentClient pbaPaymentClient;

    @Autowired
    ObjectMapper objectMapper;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("payment_creditAccountPayment", "localhost", 8886, this);

    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "payment_creditAccountPayment", consumer = "fr_caseOrchestratorService")
    public RequestResponsePact generatePactFragmentSuccess(PactDslWithProvider builder) throws JSONException, IOException {

        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("accountNumber", "test.account");
        paymentMap.put("availableBalance", "1000.00");
        paymentMap.put("accountName", "test.account.name");

        return builder
            .given("An active account has sufficient funds for a payment", paymentMap)
            .uponReceiving("A request for a payment")
            .path("/credit-account-payments")
            .method("POST")
            .headers(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            .body(objectMapper.writeValueAsString(getPaymentRequestSuccess(BigDecimal.TEN)))
            .willRespondWith()
            .status(201)
            .body(buildPBAPaymentResponseDsl("Success", "success", null, "Insufficient funds available"))
            .toPact();
    }

    @Test
    @PactVerification(fragment = "generatePactFragmentSuccess")
    public void verifyPBAPaymentPactSuccess() {
        PaymentResponse paymentResponse = pbaPaymentClient.makePaymentWithSiteId(AUTH_TOKEN, getPaymentRequestSuccess(BigDecimal.TEN));
        Assert.assertEquals("reference", paymentResponse.getReference());
    }

    private DslPart buildPBAPaymentResponseDsl(String status, String paymentStatus, String errorCode, String errorMessage) {
        return getDslPart(status, paymentStatus, errorCode, errorMessage);
    }

    static DslPart getDslPart(String status, String paymentStatus, String errorCode, String errorMessage) {
        return newJsonBody((o) -> {
            o.stringType("reference", "reference")
                .stringType("status", status)
                .minArrayLike("status_histories", 1, 1,
                    (sh) -> {
                        sh.stringMatcher("date_updated",
                                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                "2020-10-06T18:54:48.785+0000")
                            .stringMatcher("date_created",
                                "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                "2020-10-06T18:54:48.785+0000")
                            .stringValue("status", paymentStatus);
                        if (errorCode != null) {
                            sh.stringValue("error_code", errorCode);
                            sh.stringType("error_message",
                                errorMessage);
                        }
                    });
        }).build();
    }

    private PaymentRequestWithSiteID getPaymentRequestSuccess(BigDecimal amount) {
        return getPaymentRequest(amount);
    }

    static PaymentRequestWithSiteID getPaymentRequest(BigDecimal amount) {
        PaymentRequestWithSiteID expectedRequest = new PaymentRequestWithSiteID();
        expectedRequest.setService("FINREM");
        expectedRequest.setCurrency("GBP");
        expectedRequest.setAmount(amount);
        expectedRequest.setCcdCaseNumber("test.case.id");
        expectedRequest.setSiteId("AA09");
        expectedRequest.setAccountNumber("test.account");
        expectedRequest.setOrganisationName("test.organisation");
        expectedRequest.setCustomerReference("test.customer.reference");
        expectedRequest.setDescription("Financial Remedy Payment");

        FeeRequest feeRequest = new FeeRequest();
        feeRequest.setCode("test");
        feeRequest.setVersion("v1");
        feeRequest.setCalculatedAmount(amount);
        feeRequest.setVolume(1);

        expectedRequest.setFeesList(Collections.singletonList(feeRequest));
        return expectedRequest;
    }
}
