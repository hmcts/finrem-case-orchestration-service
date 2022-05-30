package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.wrapper.IdamToken;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    private final IdamAuthService idamAuthService;
    private final AuthTokenGenerator authTokenGenerator;

    @Cacheable("systemUserTokenCache")
    public String getSysUserToken() {
        return idamAuthService.getAccessToken(systemUpdateUserConfiguration.getUserName(), systemUpdateUserConfiguration.getPassword());
    }

    public IdamToken getIdamToken() {

        UserInfo user = idamAuthService.getUserInfo(getSysUserToken());

        return IdamToken.builder()
            .idamOauth2Token(getSysUserToken())
            .serviceAuthorization(getServiceAuthorization())
            .userId(user.getUid())
            .email(user.getSub())
            .roles(user.getRoles())
            .build();
    }

    private String getServiceAuthorization() {
        return authTokenGenerator.generate();
    }
}
