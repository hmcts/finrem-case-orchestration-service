package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.FeeServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.Fee;

import java.net.URI;


@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {
    private final FeeServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    public Fee getApplicationFee() {
        URI uri = buildUri();
        log.info("Inside getApplicationFee, Fee API uri : {} ", uri);
        ResponseEntity<Fee> responseEntity = restTemplate.getForEntity(uri, Fee.class);
        return responseEntity.getBody();
    }

    private URI buildUri() {
        return UriComponentsBuilder.fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi())
                .queryParam("service", serviceConfig.getService())
                .queryParam("jurisdiction1", serviceConfig.getJurisdiction1())
                .queryParam("jurisdiction2", serviceConfig.getJurisdiction2())
                .queryParam("channel", serviceConfig.getChannel())
                .queryParam("event", serviceConfig.getEvent())
                .queryParam("keyword", serviceConfig.getKeyword())
                .build().encode().toUri();
    }
}
