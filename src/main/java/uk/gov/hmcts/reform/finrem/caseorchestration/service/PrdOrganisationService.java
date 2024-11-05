package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.OrganisationApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.PrdOrganisationConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation.OrganisationsResponse;

import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.APPLICATION_SCOPED_CACHE_MANAGER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.BARRISTER_USER_CACHE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.helper.MaskHelper.maskEmail;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("java:S112")
public class PrdOrganisationService {

    private final PrdOrganisationConfiguration prdOrganisationConfiguration;
    private final RestService restService;
    private final ObjectMapper objectMapper;
    private final OrganisationApi organisationApi;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    public OrganisationsResponse retrieveOrganisationsData(String authToken) {
        return objectMapper.convertValue(
            restService.restApiGetCall(authToken, prdOrganisationConfiguration.getOrganisationsUrl()),
            OrganisationsResponse.class
        );
    }

    public OrganisationsResponse findOrganisationByOrgId(String orgId) {
        String userToken = systemUserService.getSysUserToken();

        return organisationApi.findOrganisationByOrgId(userToken, authTokenGenerator.generate(), orgId);
    }

    @Cacheable(cacheManager = APPLICATION_SCOPED_CACHE_MANAGER, cacheNames = BARRISTER_USER_CACHE)
    public Optional<String> findUserByEmail(String email, String authToken) {
        try {
            log.info("Finding user by email");
            return Optional.of(organisationApi.findUserByEmail(authToken, authTokenGenerator.generate(), email).getUserIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            log.info("Could not find user by email");
            return Optional.empty();
        } catch (FeignException exception) {
            throw new RuntimeException(email != null ? maskEmail(getStackTrace(exception), email) : "Email is not valid or null");
        }
    }
}
