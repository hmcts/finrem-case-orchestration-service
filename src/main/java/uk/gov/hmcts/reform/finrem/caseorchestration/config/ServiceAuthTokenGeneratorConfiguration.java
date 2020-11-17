package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class ServiceAuthTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi) {
        log.info("serviceAuthTokenGenerator - serviceAuthTokenGenerator - secret - {}", secret);
        log.info("serviceAuthTokenGenerator - serviceAuthTokenGenerator - microService - {}", microService);
        log.info("serviceAuthTokenGenerator - serviceAuthTokenGenerator - serviceAuthorisationApi - {}", serviceAuthorisationApi.toString());
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }
}
