package uk.gov.hmcts.reform.finrem.caseorchestration.smoketests;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan("uk.gov.hmcts.reform.finrem.caseorchestration.smoketests")
@PropertySource(value = {"classpath:application.properties"})
@PropertySource(value = {"classpath:application-${env}.properties"})
public class SmokeTestConfiguration {
}
