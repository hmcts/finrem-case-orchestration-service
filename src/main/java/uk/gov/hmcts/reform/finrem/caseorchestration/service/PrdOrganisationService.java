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
     * Retrieves a user identifier for the given email address.
     *
     * <p>This method calls the organisation API to resolve the user by email
     * using the provided authentication token. The result is cached using
     * {@code BARRISTER_USER_CACHE} under the {@code APPLICATION_SCOPED_CACHE_MANAGER}.</p>
     *
     * <p>If no user is found, an empty {@link Optional} is returned.
     * If the downstream service responds with an error other than 404 (Not Found),
     * a {@link RuntimeException} is thrown. The exception message masks the email
     * address where applicable.</p>
     *
     * @param email     the email address used to search for the user
     * @param authToken the authorisation token used for the API call
     * @return an {@link Optional} containing the user identifier if found;
     *         otherwise {@link Optional#empty()}
     *
     * @deprecated use {@link #findUserByEmail(String)} which retrieves the
     *             system user token internally instead of requiring it as a parameter
     */
    @Cacheable(cacheManager = APPLICATION_SCOPED_CACHE_MANAGER, cacheNames = BARRISTER_USER_CACHE)
    @Deprecated(forRemoval = true)
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

    /**
     * Retrieves a user identifier for the given email address using
     * the system user authentication token.
     *
     * <p>This method calls the organisation API to resolve the user by email
     * using an internally obtained system user token.</p>
     *
     * <p>If no user is found, an empty {@link Optional} is returned.
     * If the downstream service responds with an error other than 404 (Not Found),
     * a {@link RuntimeException} is thrown. The exception message masks the email
     * address where applicable.</p>
     *
     * @param email the email address used to search for the user
     * @return an {@link Optional} containing the user identifier if found;
     *         otherwise {@link Optional#empty()}
     */
    public Optional<String> findUserByEmail(String email) {
        try {
            return Optional.of(organisationApi.findUserByEmail(systemUserService.getSysUserToken(),
                authTokenGenerator.generate(), email).getUserIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new RuntimeException(email != null ? maskEmail(getStackTrace(exception), email) : "Email is not valid or null");
        }
    }

    /**
     * Retrieves the organisation identifier associated with the given user ID.
     *
     * <p>This method invokes the organisation API using a system user authentication
     * token to obtain organisation details for the supplied user ID.</p>
     *
     * <p>If no organisation is found for the given user ID (HTTP 404),
     * {@link Optional#empty()} is returned. If the downstream service responds
     * with any other error, a {@link RuntimeException} is thrown.</p>
     *
     * @param userId the unique identifier of the user whose organisation is to be retrieved
     * @return an {@link Optional} containing the organisation identifier if found;
     *         otherwise {@link Optional#empty()}
     * @throws RuntimeException if the user ID is invalid, {@code null},
     *                          or an unexpected error occurs when calling the API
     */
    public Optional<String> findOrganisationIdByUserId(String userId) {
        try {
            return Optional.of(organisationApi.findOrganisationDetailsByUser(systemUserService.getSysUserToken(),
                authTokenGenerator.generate(), userId).getOrganisationIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new RuntimeException("Given user_id is not valid or null");
        }
    }
}
