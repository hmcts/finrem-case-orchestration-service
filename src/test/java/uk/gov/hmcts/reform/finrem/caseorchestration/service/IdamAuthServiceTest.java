package uk.gov.hmcts.reform.finrem.caseorchestration.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamAuthApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdamAuthServiceTest {

    @Mock
    private IdamAuthApi idamAuthApi;
    @Mock
    private OAuth2Configuration oAuth2Configuration;
    @InjectMocks
    private IdamAuthService idamAuthService;

    @Test
    public void givenUserDetails_whenGetAccessToken_ThenReturnToken() {
        TokenResponse tokenResponse = new TokenResponse("accessToken", "expiresIn",
            "idToken", "refreshToken", "scope", "tokenType");
        when(idamAuthApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String accessToken = idamAuthService.getAccessToken("username", "password");

        assertThat(accessToken).isEqualTo("Bearer accessToken");

    }

    @Test
    public void givenUserId_whenGetUserByUserId_ThenReturnUserDetails() {
        when(idamAuthApi.getUserByUserId("authorisation", "userId"))
            .thenReturn(UserDetails.builder().id("userId").email("email@email.com").build());

        UserDetails userDetails = idamAuthService.getUserByUserId("authorisation", "userId");

        assertThat(userDetails.getId()).isEqualTo("userId");
    }

    @Test
    public void givenToken_whenGetUserInfo_ThenReturnUserInfo() {
        Map userMap = new HashMap<>();
        userMap.put("uid", "uidTest");
        when(idamAuthApi.retrieveUserInfo("token"))
            .thenReturn(userMap);

        UserInfo userInfo = idamAuthService.getUserInfo("token");

        assertThat(userInfo.getUid()).isEqualTo("uidTest");
    }
}