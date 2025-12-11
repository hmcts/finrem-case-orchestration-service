package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.SystemUpdateUserConfiguration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemUserTokenProviderTest {

    @Mock
    private SystemUpdateUserConfiguration systemUpdateUserConfiguration;

    @Mock
    private IdamAuthService idamAuthService;

    @InjectMocks
    private SystemUserTokenProvider systemUserTokenProvider;

    @Test
    void givenSysUserConfig_WhenGetSysUserToken_ThenReturnToken() {
        when(systemUpdateUserConfiguration.getUserName()).thenReturn("username");
        when(systemUpdateUserConfiguration.getPassword()).thenReturn("password");

        systemUserTokenProvider.getSysUserToken();

        verify(idamAuthService).getAccessToken("username", "password");
    }
}
