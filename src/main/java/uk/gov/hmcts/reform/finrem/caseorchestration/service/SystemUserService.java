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

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@CacheConfig(cacheNames = SYS_USER_CACHE)
public class SystemUserService {

    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final IdamAuthService idamAuthService;

    @Cacheable(cacheNames = {SYS_USER_CACHE})
    public String getSysUserToken() {
        log.info("Fetching system user token");
        return idamAuthService.getAccessToken(systemUpdateUserConfiguration.getUserName(), systemUpdateUserConfiguration.getPassword());
    }

    @CacheEvict(allEntries = true, cacheNames = {SYS_USER_CACHE})
    @Scheduled(fixedDelay = 1800000)
    public void cacheEvict() {
        log.info("Evicting system user cron cache");
    }
}
