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

    /**
     * Find a user by email address that is registered with an Organisation.
     * Suppress NotFound and BadRequest exceptions returned by API,
     * to suppress alert being sent for support follow up for user's invalid input.
     * Unhandled exceptions are logged and rethrown.
     * @param email - email address to search for
     * @param authToken - authorisation token
     * @return - Optional of user
     */
    @Cacheable(cacheManager = APPLICATION_SCOPED_CACHE_MANAGER, cacheNames = BARRISTER_USER_CACHE)
    public Optional<String> findUserByEmail(String email, String authToken) {
        try {
            if (email == null) {
                log.debug("findUserByEmail passed a null email");
                return Optional.empty();
            }
            log.debug("Finding user by email");
            return Optional.of(organisationApi.findUserByEmail(authToken, authTokenGenerator.generate(), email).getUserIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            log.debug("findUserByEmail raised a NotFound exception. Suppressed.");
            return Optional.empty();
        } catch (FeignException.BadRequest badRequestException) {
            log.debug("findUserByEmail raised a BadRequest exception. Suppressed");
            return Optional.empty();
        } catch (FeignException exception) {
            throw new RuntimeException(
                    email != null ? maskEmail(getStackTrace(exception), email) : "findUserByEmail raised an unknown exception");
        }
    }
}
