package uk.gov.hmcts.reform.finrem.caseorchestration.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class PBAServiceHealthCheck extends AbstractServiceHealthCheck {

    public PBAServiceHealthCheck(@Value("${pba.validation.health.url}") String uri, RestTemplate restTemplate) {
        super(uri, restTemplate);
    }
}
