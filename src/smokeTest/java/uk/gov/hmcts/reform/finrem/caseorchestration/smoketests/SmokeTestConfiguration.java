package uk.gov.hmcts.reform.finrem.caseorchestration.smoketests;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("uk.gov.hmcts.reform.finrem.caseorchestration")
@PropertySource("application.properties")
@PropertySource("application-${env}.properties")
public class SmokeTestConfiguration {
}
