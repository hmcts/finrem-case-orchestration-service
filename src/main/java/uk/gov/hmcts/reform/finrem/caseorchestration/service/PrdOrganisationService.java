package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.net.URI;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrdOrganisationService {

    private final PrdOrganisationConfiguration prdOrganisationConfiguration;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    public OrganisationsResponse retrieveOrganisationsData(String authToken) {
        URI uri = buildUri();
        log.info("Inside retrieveOrganisationData, PRD API uri : {}", uri);

        HttpEntity request = buildRequest(authToken);

        ResponseEntity<OrganisationsResponse> responseEntity =
            restTemplate.exchange(uri, GET, request, OrganisationsResponse.class);

        return Objects.requireNonNull(responseEntity.getBody());
    }

    private HttpEntity buildRequest(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        if (!authToken.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid user token");
        }
        headers.add("Authorization", authToken);
        headers.add("Content-Type", "application/json");
        headers.add("ServiceAuthorization", authTokenGenerator.generate());
        return new HttpEntity<>(headers);
    }

    private URI buildUri() {
        return fromHttpUrl(prdOrganisationConfiguration.getUrl() + prdOrganisationConfiguration.getApi()).build().toUri();
    }
}
