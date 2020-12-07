package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ccd.data-store.api")
public class CcdDataStoreServiceConfiguration {
    private String removeCaseRolesUrl;
}
