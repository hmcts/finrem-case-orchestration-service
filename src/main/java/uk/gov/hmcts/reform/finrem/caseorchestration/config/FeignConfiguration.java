package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import feign.Response;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class FeignConfiguration {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(200, 200, 3);
    }

    @Bean
    ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default() {
            @Override
            public Exception decode(final String methodKey, final Response response) {
                if (response.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    return new RetryableException(response.status(), "Retrying on Internal Server Error",
                        null, null);
                } else {
                    return super.decode(methodKey, response);
                }
            }
        };
    }
}
