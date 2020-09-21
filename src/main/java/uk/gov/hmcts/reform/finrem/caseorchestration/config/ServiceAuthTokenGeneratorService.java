package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Configuration
@RequiredArgsConstructor
@PropertySource(value = {"classpath:application.properties"})
public class ServiceAuthTokenGeneratorService {

    @Value("${idam.s2s-auth.url}")
    String s2sUrl;
    @Value("${idam.auth.secret}")
    String secret;
    @Value("${idam.s2s-auth.microservice}")
    String microservice;

    @Bean
    public ServiceAuthTokenGenerator createTokenGenerator() {
        ServiceAuthorisationApi serviceAuthorisationApi = Feign.builder()
            .encoder(new JacksonEncoder())
            .contract(new SpringMvcContract())
            .target(ServiceAuthorisationApi.class, s2sUrl);
        return new uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator(secret, microservice, serviceAuthorisationApi);
    }
}
