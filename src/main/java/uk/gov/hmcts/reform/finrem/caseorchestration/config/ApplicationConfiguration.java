package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.RequiredArgsConstructor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

    private final HttpConfiguration httpConfiguration;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()));
        return restTemplate;
    }

    private CloseableHttpClient httpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(httpConfiguration.getTimeout())
            .setConnectionRequestTimeout(httpConfiguration.getRequestTimeout())
            .setSocketTimeout(httpConfiguration.getReadTimeout()) // read time out
            .build();

        return HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(config)
            .build();
    }
}
