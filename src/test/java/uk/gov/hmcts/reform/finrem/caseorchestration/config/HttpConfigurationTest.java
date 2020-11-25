package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class HttpConfigurationTest extends BaseTest {

    @Autowired
    private HttpConfiguration httpConfiguration;

    @Test
    public void shouldCreateHttpConfigFromAppProperties() {
        assertThat(httpConfiguration.getReadTimeout(), is(-1));
        assertThat(httpConfiguration.getRequestTimeout(), is(-1));
        assertThat(httpConfiguration.getTimeout(), is(-1));
    }
}