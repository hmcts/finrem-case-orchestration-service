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
    private PrdOrganisationConfiguration config;

    @Test
    public void shouldCreateOrganisationConfigFromAppProperties() {
        assertThat(config.getUrl(), is("http://localhost:8090"));
        assertThat(config.getApi(), is("/refdata/external/v1/organisations"));
    }
}