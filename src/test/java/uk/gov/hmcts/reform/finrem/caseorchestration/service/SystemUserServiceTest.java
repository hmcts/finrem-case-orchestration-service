package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SYSTEM_TOKEN;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceTest {

    @Mock
    private SystemUserTokenProvider systemUserTokenProvider;

    @Mock
    private IdamAuthService idamAuthService;

    @InjectMocks
    private SystemUserService systemUserService;

    @BeforeEach
    void setUp() {
        when(systemUserTokenProvider.getSysUserToken()).thenReturn(TEST_SYSTEM_TOKEN);
    }

    @Test
    void testGetSysUserToken() {
        assertThat(systemUserService.getSysUserToken()).isEqualTo(TEST_SYSTEM_TOKEN);
    }

    @Test
    void testGetSysUserTokenUid() {
        String uid = UUID.randomUUID().toString();
        when(idamAuthService.getUserInfo(TEST_SYSTEM_TOKEN))
            .thenReturn(UserInfo.builder().uid(uid).build());

        assertThat(systemUserService.getSysUserTokenUid()).isEqualTo(uid);
    }
}
