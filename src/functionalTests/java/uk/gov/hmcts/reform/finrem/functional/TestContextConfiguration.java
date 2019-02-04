package uk.gov.hmcts.reform.finrem.functional;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Slf4j
@Configuration
@ComponentScan("uk.gov.hmcts.reform.finrem.functional")
@PropertySource(ignoreResourceNotFound = true, value = {"classpath:application-local.properties"})
public class TestContextConfiguration {


    }
