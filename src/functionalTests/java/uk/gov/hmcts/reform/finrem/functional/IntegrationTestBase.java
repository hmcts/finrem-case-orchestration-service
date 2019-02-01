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

    private String migrationUrl;


    public IntegrationTestBase() {
        this.springIntegration = new SpringIntegrationMethodRule();

    }

    @Autowired
    public void migrationUrl(@Value("${migration.uri}")
                                                    String migrationUrl) {
        this.migrationUrl = migrationUrl;
        RestAssured.baseURI = migrationUrl;
    }
}
