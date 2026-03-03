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
            String stackTrace = getStackTrace(exception);
            throw new RuntimeException(email != null ? maskEmail(stackTrace, email) : "Email is not valid or null");
        }
    }

    /**
     * Retrieves the organisation identifier associated with the given user ID.
     *
     * <p>This method calls the Organisation API to obtain organisation details
     * for the specified user. If no organisation is found (HTTP 404), an empty
     * {@link Optional} is returned. Any other API error results in a
     * {@link RuntimeException}.
     *
     * @param userId   the unique identifier of the user whose organisation is to be retrieved
     * @param authToken the authorisation token used to authenticate the API request
     * @return an {@link Optional} containing the organisation identifier if found;
     *         otherwise {@link Optional#empty()} when the user is not associated
     *         with an organisation
     *
     * @throws RuntimeException if the user ID is invalid, null, or an unexpected
     *                          error occurs while calling the Organisation API
     */
    public Optional<String> findOrganisationIdByUserId(String userId, String authToken) {
        try {
            return Optional.of(organisationApi.findOrganisationDetailsByUserr(authToken,
                authTokenGenerator.generate(), userId).getOrganisationIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new RuntimeException("Given user_id is not valid or null");
        }
    }
}
