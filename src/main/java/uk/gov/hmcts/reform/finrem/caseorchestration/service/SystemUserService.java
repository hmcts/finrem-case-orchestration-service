package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;

import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.REQUEST_SCOPED_CACHE_MANAGER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.config.CacheConfiguration.SYS_USER_CACHE;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final IdamAuthService idamAuthService;

    @Cacheable(cacheManager = REQUEST_SCOPED_CACHE_MANAGER, cacheNames = SYS_USER_CACHE)
    public String getSysUserToken() {
        return idamAuthService.getAccessToken(systemUpdateUserConfiguration.getUserName(), systemUpdateUserConfiguration.getPassword());
    }
}
