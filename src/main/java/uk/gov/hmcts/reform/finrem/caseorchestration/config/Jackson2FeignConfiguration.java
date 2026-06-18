package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Jackson2FeignConfiguration {

    @Bean
    public Decoder feignDecoder() {
        com.fasterxml.jackson.databind.ObjectMapper jackson2Mapper =
            new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule())
                .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return (response, type) -> {
            if (type == String.class) {
                return feign.Util.toString(response.body().asReader(feign.Util.UTF_8));
            }
            return new feign.jackson.JacksonDecoder(jackson2Mapper).decode(response, type);
        };
    }
}