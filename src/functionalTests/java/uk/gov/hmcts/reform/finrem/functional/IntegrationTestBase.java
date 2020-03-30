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

    public static String caseOrchestrationUrl;
    public static String serviceAuthUrl;

    @Rule
    public SpringIntegrationMethodRule springIntegration;

    @Autowired
    protected FunctionalTestUtils utils;

    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegrationMethodRule();
    }

    public static void setServiceAuthUrlAsBaseUri() {
        RestAssured.baseURI = serviceAuthUrl;
    }

    @Autowired
    public void caseOrchestrationUrl(@Value("${case.orchestration.api}")
                                             String caseOrchestrationUrl) {
        this.caseOrchestrationUrl = caseOrchestrationUrl;
        RestAssured.baseURI = caseOrchestrationUrl;
    }

    @Autowired
    public void serviceAuthUrl(@Value("${idam.s2s-auth.url}") String serviceAuthUrl) {
        this.serviceAuthUrl = serviceAuthUrl;
    }
}
