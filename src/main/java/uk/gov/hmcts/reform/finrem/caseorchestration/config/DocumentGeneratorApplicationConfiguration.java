package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentGeneratorApplicationConfiguration {

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
