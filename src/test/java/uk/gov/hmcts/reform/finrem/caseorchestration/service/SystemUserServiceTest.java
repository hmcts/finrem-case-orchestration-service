package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SystemUserServiceTest {

    public static final String AUTH_TOKEN = "tokien:)";

    @Mock
    private SystemUpdateUserConfiguration systemUpdateUserConfiguration;
    @Mock
    private IdamAuthService idamAuthService;
    @InjectMocks
    private SystemUserService systemUserService;

    @Test
    public void givenSysUserConfig_WhenGetSysUserToken_ThenReturnToken() {
        when(systemUpdateUserConfiguration.getUserName()).thenReturn("username");
        when(systemUpdateUserConfiguration.getPassword()).thenReturn("password");

        systemUserService.getSysUserToken();

        verify(idamAuthService).getAccessToken("username", "password");
    }
}