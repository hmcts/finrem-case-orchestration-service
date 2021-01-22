package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class PrdOrganisationConfigurationTest extends BaseTest {

    @Autowired
    private PrdConfiguration config;

    @Test
    public void shouldCreateConfigFromAppProperties() {
        assertThat(config.getUsername(), is("http://localhost:8090/refdata/external/v1/organisations"));
        assertThat(config.getPassword(), is("http://localhost:8090/refdata/external/v1/organisations"));
    }
}