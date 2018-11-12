package uk.gov.hmcts.probate.functional;

import io.restassured.RestAssured;
import net.thucydides.junit.spring.SpringIntegration;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.probate.functional.util.TestUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestContextConfiguration.class)
public abstract class IntegrationTestBase {

    @Autowired
    protected TestUtils utils;

    private String finServiceUrl;
    public static String evidenceManagementUrl;

    @Autowired
    public void finServiceUrl(@Value("${fin.service.base.url}") String finServiceUrl) {
        this.finServiceUrl = finServiceUrl;
        System.out.println("Test url from test..." + finServiceUrl);
        RestAssured.baseURI = finServiceUrl;
    }

    @Rule
    public SpringIntegration springIntegration;

    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegration();

    }
}
