package uk.gov.hmcts.reform.finrem.functional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

@Configuration
@ComponentScan("uk.gov.hmcts.reform.finrem.functional")
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client", "uk.gov.hmcts.reform.finrem"},
    basePackageClasses = ServiceAuthorisationApi.class)
@PropertySource(value = {"classpath:application.properties"})
@PropertySource(value = {"classpath:application-${env}.properties"})
@Slf4j
public class TestContextConfiguration {

    @Bean
    public ServiceAuthTokenGenerator serviceAuthTokenGenerator(@Value("${idam.s2s-auth.url}")
                                                                   String s2sUrl,
                                                               @Value("${idam.auth.secret}")
                                                                   String secret,
                                                               @Value("${idam.s2s-auth.microservice}")
                                                                   String microservice) {
        ServiceAuthorisationApi serviceAuthorisationApi = Feign.builder()
            .encoder(new JacksonEncoder())
            .contract(new SpringMvcContract())
            .target(ServiceAuthorisationApi.class, s2sUrl);
        return new ServiceAuthTokenGenerator(secret, microservice, serviceAuthorisationApi);
    }

    @Bean
    public CoreCaseDataApi getCoreCaseDataApi(@Value("${core_case_data.api.url}") String coreCaseDataApiUrl) {
        return Feign.builder()
            .requestInterceptor(requestInterceptor())
            .encoder(new JacksonEncoder())
            .decoder(feignDecoder())
            .contract(new SpringMvcContract())
            .target(CoreCaseDataApi.class, coreCaseDataApiUrl);
    }

    @Bean
    public Decoder feignDecoder() {
        return new feign.jackson.JacksonDecoder(objectMapper());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.json.JsonMapper()
            .builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            if (template.request().httpMethod() == Request.HttpMethod.POST) {
                template.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            }
        };
    }
}
