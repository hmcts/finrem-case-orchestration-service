package uk.gov.hmcts.reform.finrem.functional;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.functional.util.FunctionalTestUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = uk.gov.hmcts.reform.finrem.functional.TestContextConfiguration.class)
public abstract class IntegrationTestBase {


    @Rule
    public SpringIntegrationMethodRule springIntegration;

    @Autowired
    protected FunctionalTestUtils utils;

    public static String caseOrchestrationUrl;

    public static String serviceAuthUrl;

    public static String documentGeneratorServiceUrl;

    public static String notificationServiceUrl;

    public static String paymentServiceUrl;


    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegrationMethodRule();
    }

    @Autowired
    public void caseOrchestrationUrl(@Value("${case.orchestration.api}")
                                                    String caseOrchestrationUrl) {
        this.caseOrchestrationUrl = caseOrchestrationUrl;
    }

    public static void setDocumentGeneratorServiceUrlAsBaseUri() {
        RestAssured.baseURI = documentGeneratorServiceUrl;
    }


    @Autowired
    public void notificationServiceUrl(@Value("${notification.uri}")
                                                    String notificationServiceUrl) {
        this.notificationServiceUrl = notificationServiceUrl;

    }

    public static void setNotificationServiceUrlAsBaseUri() {
        RestAssured.baseURI = notificationServiceUrl;
    }

    @Autowired
    public void paymentServiceUrl(@Value("${payment_api_url}")
                                               String paymentServiceUrl) {
        this.paymentServiceUrl = paymentServiceUrl;

    }

    public static void setPaymentServiceUrlUrlAsBaseUri() {
        RestAssured.baseURI = notificationServiceUrl;
    }


    @Autowired
    public void serviceAuthUrl(@Value("${idam.s2s-auth.url}")String serviceAuthUrl) {
        this.serviceAuthUrl = serviceAuthUrl;

    }

    public static void setServiceAuthUrlAsBaseUri() {
        RestAssured.baseURI = serviceAuthUrl;
    }

}
