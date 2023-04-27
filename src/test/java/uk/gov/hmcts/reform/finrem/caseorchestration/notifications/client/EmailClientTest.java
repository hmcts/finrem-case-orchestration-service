package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.properties")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Slf4j
@Category(IntegrationTest.class)
public class EmailClientTest extends BaseTest {

    @Value("${uk.gov.notify.api.key}")
    private String apiKey;

    @Autowired
    private EmailClient emailClient;

    @Test
    public void shouldHaveTheCorrectApiKey() {
        assertEquals(apiKey, emailClient.getApiKey());
    }
}