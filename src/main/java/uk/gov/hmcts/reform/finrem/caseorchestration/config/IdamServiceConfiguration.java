package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "idam.api")
public class IdamServiceConfiguration {
    private String url;
    private String details;
}
