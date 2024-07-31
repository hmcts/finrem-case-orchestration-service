package uk.gov.hmcts.reform.finrem.caseorchestration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.task.ScheduledTaskRunner;

@SpringBootApplication(scanBasePackages = {
    "uk.gov.hmcts.reform.finrem", "uk.gov.hmcts.reform.bsp.common", "uk.gov.hmcts.reform.ccd.document.am.feign"
})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client", "uk.gov.hmcts.reform.finrem",
    "uk.gov.hmcts.reform.ccd.client"},
    basePackageClasses = {CaseDocumentClientApi.class, ServiceAuthorisationApi.class})
@EnableCaching
@EnableScheduling
@RequiredArgsConstructor
public class CaseOrchestrationApplication  implements CommandLineRunner {

    public static final String TASK_NAME = "TASK_NAME";

    private final ScheduledTaskRunner taskRunner;

    public static void main(String[] args) {
        final var application = new SpringApplication(CaseOrchestrationApplication.class);
        final var instance = application.run(args);

        if (System.getenv(TASK_NAME) != null) {
            instance.close();
        }
    }

    @Override
    public void run(String... args) {
        if (System.getenv(TASK_NAME) != null) {
            taskRunner.run(System.getenv(TASK_NAME));
        }
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
