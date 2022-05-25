package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.wrapper.IdamToken;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration userConfig;
    private final IdamAuthService idamClient;

    @Cacheable("systemUserTokenCache")
    public String getSysUserToken() {
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    public IdamToken getIdamToken() {

        UserInfo user = idamClient.getUserInfo(getSysUserToken());

        return IdamToken.builder()
            .idamOauth2Token(getSysUserToken())
            .serviceAuthorization(idamClient.getServiceAuthorization())
            .userId(user.getUid())
            .email(user.getSub())
            .roles(user.getRoles())
            .build();
    }

}
