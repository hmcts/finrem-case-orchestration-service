package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Narrow Jackson 3 exception: {@code uk.gov.hmcts.reform.ccd.client.CoreCaseDataConfiguration}
 * (from core-case-data-store-client) requires a tools.jackson.databind.ObjectMapper bean
 * in its Feign client context. This mapper is isolated to that Feign context only and is
 * not used by the application's own HTTP converters, which remain on Jackson 2.
 */
@Configuration
public class CcdClientJacksonConfig {

    @Bean
    public ObjectMapper ccdClientObjectMapper() {
        return JsonMapper.builder().build();
    }
}
