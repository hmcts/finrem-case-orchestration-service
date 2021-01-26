package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthAutoConfiguration;

@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.finrem", "uk.gov.hmcts.reform.bsp.common", "uk.gov.hmcts.reform.idam.client", "uk.gov.hmcts.reform.authorisation"
})
@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.reform.finrem", "uk.gov.hmcts.reform.logging.appinsights",
        "uk.gov.hmcts.reform.bsp.common"
    },
    exclude = {
        ServiceAuthAutoConfiguration.class, DataSourceAutoConfiguration.class
})
public class CaseOrchestrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseOrchestrationApplication.class, args);
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(false);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setMaxPayloadLength(10240);
        return loggingFilter;
    }
}
