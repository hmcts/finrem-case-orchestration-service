package uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.pba.validation.PBAValidationResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.config.PBAValidationServiceConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.error.InvalidTokenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.payments.model.pba.validation.PBAOrganisationResponse;

import java.net.URI;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public class PBAValidationClient {

    protected static final String USER_EMAIL = "UserEmail";
    private final IdamService idamService;
    private final PBAValidationServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    @SuppressWarnings("java:S3740")
    public PBAValidationResponse isPBAValid(String authToken, String pbaNumber) {
        String emailId = idamService.getUserEmailId(authToken);
        URI uri = buildUri();
        log.info("Inside isPBAValid, PRD API uri : {}, emailId : {}", uri, emailId);
        try {
            HttpEntity request;
            request = buildRequest(authToken, emailId);
            ResponseEntity<PBAOrganisationResponse> responseEntity = restTemplate.exchange(uri, GET,
                request, PBAOrganisationResponse.class);
            PBAOrganisationResponse pbaOrganisationResponse = Objects.requireNonNull(responseEntity.getBody());
            log.info("pbaOrganisationEntityResponse : {}", pbaOrganisationResponse);
            boolean isValid = pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount()
                .contains(pbaNumber);
            return PBAValidationResponse.builder().pbaNumberValid(isValid).build();
        } catch (HttpClientErrorException ex) {
            log.info("HttpClientErrorException caught", ex);
            return PBAValidationResponse.builder().build();
        }
    }

    @SuppressWarnings("java:S3740")
    private HttpEntity buildRequest(String authToken, String emailId) {
        HttpHeaders headers = new HttpHeaders();
        if (!authToken.matches("^Bearer .+")) {
            throw new InvalidTokenException("Invalid user token");
        }
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        headers.add(USER_EMAIL, emailId);
        return new HttpEntity<>(headers);
    }

    private URI buildUri() {
        return fromHttpUrl(serviceConfig.getUrl() + serviceConfig.getApi())
            .build().toUri();
    }
}
