package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.IdamServiceConfiguration;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.FR_COURT_ADMIN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.ROLES;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"java:S3740", "java:S1905", "java:S4276"})
public class IdamService {

    private static final Function<IdamServiceConfiguration, URI> uriSupplier =
        serviceConf -> fromHttpUrl(serviceConf.getUrl() + serviceConf.getApi()).build().toUri();
    private static final Function<String, HttpEntity> buildAuthRequest = authToken -> {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION_HEADER, authToken);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    };
    private static final Function<ResponseEntity<Map>, Boolean> isAdmin =
        responseEntity -> List.class.cast(responseEntity.getBody().get(ROLES)).stream()
            .anyMatch(role -> role.equals(FR_COURT_ADMIN));
    private static final Function<ResponseEntity<Map>, String> userFullName = responseEntity -> {
        Map body = responseEntity.getBody();
        return body.get("forename") + " " + body.get("surname");
    };
    private static final Function<ResponseEntity<Map>, String> userSurname = responseEntity -> {
        Map body = responseEntity.getBody();
        return (String) body.get("surname");
    };
    private static final Function<ResponseEntity<Map>, String> userId = responseEntity -> {
        Map body = responseEntity.getBody();
        return (String) body.get("id");
    };
    private final IdamServiceConfiguration serviceConfig;
    private final RestTemplate restTemplate;

    private static final Function<ResponseEntity<Map>, String> email = responseEntity -> {
        Map body = responseEntity.getBody();
        return (String) body.get("email").toString().toLowerCase();
    };

    public boolean isUserRoleAdmin(String authToken) {
        return isAdmin.apply(restTemplate.exchange(uriSupplier.apply(serviceConfig), HttpMethod.GET,
            buildAuthRequest.apply(authToken), Map.class));
    }

    public String getIdamFullName(String authorisationToken) {
        return userFullName.apply(restTemplate.exchange(uriSupplier.apply(serviceConfig), HttpMethod.GET,
            buildAuthRequest.apply(authorisationToken), Map.class));
    }

    public String getIdamSurname(String authorisationToken) {
        return userSurname.apply(restTemplate.exchange(uriSupplier.apply(serviceConfig), HttpMethod.GET,
            buildAuthRequest.apply(authorisationToken), Map.class));
    }

    public String getIdamUserId(String authorisationToken) {
        return userId.apply(restTemplate.exchange(uriSupplier.apply(serviceConfig), HttpMethod.GET,
            buildAuthRequest.apply(authorisationToken), Map.class));
    }

    public String getUserEmailId(String authorisationToken) {
        return email.apply(restTemplate.exchange(uriSupplier.apply(serviceConfig), HttpMethod.GET,
            buildAuthRequest.apply(authorisationToken), Map.class));
    }
}

