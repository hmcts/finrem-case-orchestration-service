package uk.gov.hmcts.reform.finrem.finremcaseprogression.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.config.FeeServiceConfiguration;
import uk.gov.hmcts.reform.finrem.finremcaseprogression.model.fee.Fee;

import java.net.URI;


@Service
@RequiredArgsConstructor
public class FeeService {
    private final FeeServiceConfiguration feeServiceConfiguration;
    private final RestTemplate restTemplate;


    public Fee getApplicationFee() {
        URI uri = buildUri();
        ResponseEntity<Fee> responseEntity = restTemplate.getForEntity(uri, Fee.class);
        return responseEntity.getBody();
    }

    private URI buildUri() {
        return UriComponentsBuilder.fromHttpUrl(feeServiceConfiguration.getUrl() + feeServiceConfiguration.getApi())
                .queryParam("service", feeServiceConfiguration.getService())
                .queryParam("jurisdiction1", feeServiceConfiguration.getJurisdiction1())
                .queryParam("jurisdiction2", feeServiceConfiguration.getJurisdiction2())
                .queryParam("channel", feeServiceConfiguration.getChannel())
                .queryParam("event", feeServiceConfiguration.getEvent())
                .queryParam("keyword", feeServiceConfiguration.getKeyword())
                .build().encode().toUri();
    }
}
