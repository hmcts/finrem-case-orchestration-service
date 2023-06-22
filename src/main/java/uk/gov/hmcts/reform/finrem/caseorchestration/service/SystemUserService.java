package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {
    public static final int ONE_HOUR = 1000 * 60 * 60;

    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final IdamAuthService idamAuthService;
    private String cachedToken;
    private final AtomicInteger atomicInteger = new AtomicInteger(1);

    @Retryable(backoff = @Backoff(delay = 15000L, multiplier = 1.0, random = true))
    public String getSysUserToken() {
        String idamOauth2Token;
        if (StringUtils.isEmpty(cachedToken)) {
            log.info("No cached IDAM token found, requesting from IDAM service.");
            log.info("Attempting to obtain token, retry attempt {}", atomicInteger.getAndIncrement());
            idamOauth2Token =  getIdamOauth2Token();
        } else {
            atomicInteger.set(1);
            log.info("Using cached IDAM token.");
            idamOauth2Token = cachedToken;
        }
        return idamOauth2Token;
    }

    @Scheduled(fixedRate = ONE_HOUR)
    private void evictCacheAtIntervals() {
        log.info("Evicting idam token cache");
        cachedToken = null;
    }

    private String getIdamOauth2Token() {
        cachedToken = idamAuthService.getAccessToken(systemUpdateUserConfiguration.getUserName(), systemUpdateUserConfiguration.getPassword());
        return cachedToken;
    }
}
