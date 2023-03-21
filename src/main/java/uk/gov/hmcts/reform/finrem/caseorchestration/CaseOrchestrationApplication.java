package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;

@SpringBootApplication(scanBasePackages = {
    "uk.gov.hmcts.reform.finrem", "uk.gov.hmcts.reform.bsp.common"
})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client", "uk.gov.hmcts.reform.finrem"},
    basePackageClasses = {CaseDocumentClientApi.class, ServiceAuthorisationApi.class})
@EnableCaching
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
