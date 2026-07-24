package uk.gov.hmcts.reform.finrem.caseorchestration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.task.ScheduledTaskRunner;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;

@SpringBootApplication(
    scanBasePackages = {
        "uk.gov.hmcts.reform.finrem",
        "uk.gov.hmcts.reform.bsp.common",
        "uk.gov.hmcts.reform.ccd.document.am.feign"
    },
    exclude = {
        uk.gov.hmcts.reform.sendletter.SendLetterAutoConfiguration.class,
        // This is a temporary workaround until SpringDoc releases a version that's fully compatible with Spring Boot 4.1.
        // You can check if a newer version fixes it:
        // `find ~/.gradle -path "*/springdoc*.jar" | grep -v sources | head -1 | xargs jar tf | grep -i "HateoasConfig"`
        // If a newer SpringDoc version removes the HateoasProperties reference, you can remove the exclude. Until then keep it.
        org.springdoc.core.configuration.SpringDocHateoasConfiguration.class
    })

@EnableFeignClients(
    basePackages = {"uk.gov.hmcts.reform.finrem"},
    basePackageClasses = {
        ServiceAuthorisationApi.class,    // uk.gov.hmcts.reform.authorisation
        IdamApi.class,                    // uk.gov.hmcts.reform.idam.client
        CoreCaseDataApi.class,            // uk.gov.hmcts.reform.ccd.client
        SendLetterApi.class,            // uk.gov.hmcts.reform.sendletter
        CaseDocumentClientApi.class,      // uk.gov.hmcts.reform.ccd.document.am.feign
    }
)
@EnableCaching
@EnableRetry
@EnableScheduling
@RequiredArgsConstructor
public class CaseOrchestrationApplication implements CommandLineRunner {

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
    @Profile("local")
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
