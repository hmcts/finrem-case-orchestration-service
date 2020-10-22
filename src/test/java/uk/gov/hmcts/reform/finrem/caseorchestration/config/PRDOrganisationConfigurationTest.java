package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestPropertySource(locations = "/application.properties")
public class PRDOrganisationConfigurationTest {

    @Autowired
    private PRDOrganisationConfiguration config;

    @Autowired
    protected RestTemplate restTemplate;

    protected MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldCreateOrganisationConfigFromAppProperties() {
        assertThat(config.getUrl(), is("http://localhost:8090"));
        assertThat(config.getApi(), is("/refdata/external/v1/organisations"));
    }
}