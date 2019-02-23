package uk.gov.hmcts.reform.finrem.functional;


import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;


@Slf4j
@Configuration
@ComponentScan("uk.gov.hmcts.reform.finrem.functional")
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@PropertySource(value = {"classpath:application.properties"})
@PropertySource(value = {"classpath:application-${env}.properties"})
public class TestContextConfiguration {


    public ServiceAuthTokenGenerator serviceAuthTokenGenerator(@Value("${idam.s2s-auth.url}")
                                                                       String s2sUrl,
                                                               @Value("${idam.auth.secret}")
                                                                       String secret,
                                                               @Value("${idam.s2s-auth.microservice}")
                                                                       String microservice) {
        final ServiceAuthorisationApi serviceAuthorisationApi = Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
        return new ServiceAuthTokenGenerator(secret, microservice, serviceAuthorisationApi);
    }


}
