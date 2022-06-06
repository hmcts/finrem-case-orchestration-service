package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.finrem.caseorchestration.wrapper.IdamToken;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SystemUserServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    private SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    @Mock
    private IdamAuthService idamAuthService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private SystemUserService systemUserService;

    @Test
    public void givenSysUserConfig_WhenGetSysUserToken_ThenReturnToken() {
        when(systemUpdateUserConfiguration.getUserName()).thenReturn("username");
        when(systemUpdateUserConfiguration.getPassword()).thenReturn("password");

        systemUserService.getSysUserToken();

        verify(idamAuthService).getAccessToken("username", "password");
    }

    @Test
    public void givenSysUserConfig_WhenGetIdamToken_ThenReturnToken() {
        when(idamAuthService.getUserInfo(AUTH_TOKEN))
            .thenReturn(UserInfo.builder()
                .uid("uid")
                .sub("sub@mail.com")
                .roles(Collections.singletonList("role"))
                .build());

        IdamToken idamToken = systemUserService.getIdamToken(AUTH_TOKEN);

        assertThat(idamToken.getIdamOauth2Token()).isEqualTo(AUTH_TOKEN);
        assertThat(idamToken.getUserId()).isEqualTo("uid");
        assertThat(idamToken.getEmail()).isEqualTo("sub@mail.com");
    }
}