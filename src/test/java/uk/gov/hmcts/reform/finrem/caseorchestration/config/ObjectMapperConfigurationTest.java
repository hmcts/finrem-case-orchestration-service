package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ObjectMapperConfigurationTest {

    /**
     * Made a Spy so that the service is not mocked but calls like
     * <code>when(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled()).thenReturn(false)</code>
     * are possible.
     */
    @Spy
    FeatureToggleService featureToggleService;

    ObjectMapperConfiguration objectMapperConfiguration;

    @Before
    public void setup() {
        objectMapperConfiguration = new ObjectMapperConfiguration(featureToggleService);
    }

    @Test
    public void createObjectMapper() {
        assertThat(objectMapperConfiguration.objectMapper(), is(notNullValue()));
    }
}
