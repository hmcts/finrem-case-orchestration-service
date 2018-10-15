package uk.gov.hmcts.reform.finrem.finremcaseprogression.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fees.api")
public class FeeServiceConfiguration {

    private String url;
    private String api;
    private String channel;
    private String event;
    private String jurisdiction1;
    private String jurisdiction2;
    private String keyword;
    private String service;
}
