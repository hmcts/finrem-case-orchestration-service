package uk.gov.hmcts.reform.finrem.caseorchestration.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DocumentGeneratorServiceHealthCheck extends AbstractServiceHealthCheck {

    @Autowired
    public DocumentGeneratorServiceHealthCheck(
        @Value("${document.generator.service.api.health.url}") String uri,
        RestTemplate restTemplate) {
        super(uri, restTemplate);
    }
}
