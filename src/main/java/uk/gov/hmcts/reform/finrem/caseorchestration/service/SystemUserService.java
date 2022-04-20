package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration userConfig;
    private final IdamAuthService idamClient;

    @Cacheable("systemUserTokenCache")
    public String getSysUserToken() {
        log.info("Sending access token request to idam client for user {}:{}",
            userConfig.getUserName(), userConfig.getPassword());
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

//    public String getUserId(String userToken) {
//        return idamClient.getUserInfo(userToken).getUid();
//    }

}
