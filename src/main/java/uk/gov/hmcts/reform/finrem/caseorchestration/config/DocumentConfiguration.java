package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "document")
public class DocumentConfiguration {
    private String miniFormTemplate;
    private String miniFormFileName;
    private String rejectedOrderTemplate;
    private String rejectedOrderFileName;
    private String coversheetTemplate;
    private String coversheetFileName;
    private String rejectedOrderDocType;
    private String formGTemplate;
    private String formGFileName;
}
