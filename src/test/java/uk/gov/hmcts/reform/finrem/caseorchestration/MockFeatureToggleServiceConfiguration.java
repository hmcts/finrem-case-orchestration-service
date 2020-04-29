package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

@Profile("test-mock-feature-toggle-service")
@Configuration
public class MockFeatureToggleServiceConfiguration {

    @Bean
    @Primary
    public FeatureToggleService featureToggleService() {
        return Mockito.spy(FeatureToggleService.class);
    }
}
