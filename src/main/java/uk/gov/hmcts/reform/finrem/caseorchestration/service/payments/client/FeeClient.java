package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ApplicationType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.fee.FeeResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.config.FeeServiceConfiguration;

import java.net.URI;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeClient {

    private final FeeServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    public FeeResponse getApplicationFee(ApplicationType application, String typeOfApplication) {
        URI uri = isSchedule1Application(typeOfApplication) ? buildSchedule1Uri(application) : buildUri(application);
        log.info("Inside getApplicationFee, FeeResponse API uri : {} ", uri);
        ResponseEntity<FeeResponse> response = restTemplate.getForEntity(uri, FeeResponse.class);
        log.info("Fee response : {} ", response);
        return response.getBody();
    }

    private URI buildUri(ApplicationType application) {
        return fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi())
            .queryParam("service", serviceConfig.getService())
            .queryParam("jurisdiction1", serviceConfig.getJurisdiction1())
            .queryParam("jurisdiction2", serviceConfig.getJurisdiction2())
            .queryParam("channel", serviceConfig.getChannel())
            .queryParam("event", application == ApplicationType.CONSENTED
                ? serviceConfig.getConsentedEvent() : serviceConfig.getContestedEvent())
            .queryParam("keyword", getKeyword(application))
            .build()
            .encode()
            .toUri();
    }

    private URI buildSchedule1Uri(ApplicationType application) {
        return fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi())
            .queryParam("service", serviceConfig.getSchedule1Service())
            .queryParam("jurisdiction1", serviceConfig.getJurisdiction1())
            .queryParam("jurisdiction2", serviceConfig.getJurisdiction2())
            .queryParam("channel", serviceConfig.getChannel())
            .queryParam("event", application == ApplicationType.CONSENTED
                ? serviceConfig.getConsentedEvent() : serviceConfig.getContestedEvent())
            .queryParam("keyword", serviceConfig.getSchedule1Keyword())
            .build()
            .encode()
            .toUri();
    }

    @SuppressWarnings("java:S5411")
    public String getKeyword(ApplicationType application) {
        log.info("Inside getKeyword for application type: {} and with use new keywords set to {}",
            application, serviceConfig.getFeePayNewKeywords());
        if (application == ApplicationType.CONSENTED) {
            return serviceConfig.getConsentedKeyword();
        } else if (serviceConfig.getFeePayNewKeywords()) {
            return serviceConfig.getContestedNewKeyword();
        } else {
            return serviceConfig.getContestedKeyword();
        }
    }

    private boolean isSchedule1Application(String typeOfApplication) {
        return typeOfApplication != null && typeOfApplication.equals("Under paragraph 1 or 2 of schedule 1 children act 1989");
    }
}
