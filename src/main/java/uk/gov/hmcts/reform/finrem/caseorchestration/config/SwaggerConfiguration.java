package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Financial Remedy Case Orchestration Service API")
                .description("Given a case data, This service will orchestrate the financial remedy features "
                    + "like notifications, fee lookUp and DocumentGenerator"));
    }
}
