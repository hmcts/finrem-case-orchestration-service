package uk.gov.hmcts.reform.finrem.caseorchestration.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CoreCaseDataHealthCheck extends AbstractServiceHealthCheck {

    @Autowired
    public CoreCaseDataHealthCheck(
        @Value("${core_case_data.api.health.url}") String uri,
        RestTemplate restTemplate) {
        super(uri, restTemplate);
    }
}
