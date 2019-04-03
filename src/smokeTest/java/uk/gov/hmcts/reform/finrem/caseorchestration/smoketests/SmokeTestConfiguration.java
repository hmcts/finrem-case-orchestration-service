package uk.gov.hmcts.reform.finrem.caseorchestration.smoketests;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("uk.gov.hmcts.reform.finrem.caseorchestration.smoketests")
@PropertySource("application.properties")
public class SmokeTestConfiguration {
}
