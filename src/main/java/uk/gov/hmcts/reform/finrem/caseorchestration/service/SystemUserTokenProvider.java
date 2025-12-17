package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;

import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.SYS_USER_CACHE;

/**
 * Service class responsible for providing and managing the system update user's authentication token.
 *
 * <p>This class uses caching to store the token, which is retrieved from the IDAM authentication service.
 * The cache is configured with the name defined in {@code SYS_USER_CACHE}.
 *
 * <p>It uses Lombok annotations for logging ({@code @Slf4j}) and constructor injection
 * ({@code @RequiredArgsConstructor(onConstructor_ = {@Autowired})}).
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@CacheConfig(cacheNames = SYS_USER_CACHE)
public class SystemUserTokenProvider {

    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final IdamAuthService idamAuthService;

    /**
     * Retrieves the system user's authentication token.
     *
     * <p>The result is cached using {@code @Cacheable} with the name defined in {@code SYS_USER_CACHE}.
     * If the token is already in the cache, it is returned immediately; otherwise, it is fetched
     * by calling {@link IdamAuthService#getAccessToken(String, String)} using the credentials
     * from {@link SystemUpdateUserConfiguration}.
     *
     * @return The system user's authentication token as a String.
     */
    @Cacheable(cacheNames = {SYS_USER_CACHE})
    public String getSysUserToken() {
        log.info("Fetching system user token");
        return idamAuthService.getAccessToken(systemUpdateUserConfiguration.getUserName(), systemUpdateUserConfiguration.getPassword());
    }

    /**
     * Evicts all entries from the system user token cache.
     *
     * <p>This method is scheduled to run periodically with a fixed delay of 1,800,000 milliseconds
     * (30 minutes) using {@code @Scheduled(fixedDelay = 1800000)} to ensure the token is refreshed
     * before its potential expiry.
     */
    @CacheEvict(allEntries = true, cacheNames = {SYS_USER_CACHE})
    @Scheduled(fixedDelay = 1800000)
    public void cacheEvict() {
        log.info("Evicting system user cron cache");
    }
}
