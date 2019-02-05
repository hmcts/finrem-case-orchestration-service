package uk.gov.hmcts.reform.finrem.functional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@ComponentScan("uk.gov.hmcts.reform.finrem.functional")
@PropertySource(ignoreResourceNotFound = true, value = {"classpath:application-local.properties"})
public class TestContextConfiguration {


}
