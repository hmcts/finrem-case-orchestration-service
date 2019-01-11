package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.Mockito.mock;

@Profile("test")
@Configuration
public class MyMockConfiguration {

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator() {
        return mock(AuthTokenGenerator.class);
    }
}
