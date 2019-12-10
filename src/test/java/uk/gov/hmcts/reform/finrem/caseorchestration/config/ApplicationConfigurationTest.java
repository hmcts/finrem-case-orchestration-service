package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class ApplicationConfigurationTest {

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Test
    public void createRestTemplate() {
        assertThat(applicationConfiguration.restTemplate(), is(notNullValue()));
    }

    @Test
    public void createObjectMapper() {
        assertThat(applicationConfiguration.objectMapper(Jackson2ObjectMapperBuilder.json()), is(notNullValue()));
    }
}
