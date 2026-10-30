package uk.gov.hmcts.reform.finrem.caseorchestration.notifications.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;
import uk.gov.hmcts.reform.finrem.caseorchestration.integrationtest.IntegrationTest;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
@Category(IntegrationTest.class)
public class EmailClientTest extends BaseTest {

    @Value("${uk.gov.notify.api.key}")
    private String apiKey;

    @Autowired
    private EmailClient emailClient;

    @Test
    public void shouldHaveTheCorrectApiKey() {
        // Notify API keys are 36 characters long, so we can check the last 36 characters of the key to ensure it is correct.
        String extractedApiKey = apiKey.substring(Math.max(0, apiKey.length() - 36));
        assertEquals(extractedApiKey, emailClient.getApiKey());
    }
}
