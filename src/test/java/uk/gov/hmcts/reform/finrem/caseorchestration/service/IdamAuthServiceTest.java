package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamAuthApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamOidcApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.wrapper.IdamToken;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class IdamAuthServiceTest {

    @Mock
    private IdamAuthApi idamAuthApi;
    @Mock
    private IdamOidcApi idamOidcApi;
    @Mock
    private OAuth2Configuration oAuth2Configuration;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private IdamAuthService idamAuthService;

    @Test
    void givenUserDetails_whenGetAccessToken_ThenReturnToken() {
        TokenResponse tokenResponse = new TokenResponse(AUTH_TOKEN, "expiresIn",
            "idToken", "refreshToken", "scope", "tokenType");
        when(idamOidcApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String accessToken = idamAuthService.getAccessToken("username", "password");

        assertThat(accessToken).isEqualTo("Bearer " + AUTH_TOKEN);
    }

    @Test
    void givenUserId_whenGetUserByUserId_ThenReturnUserDetails() {
        when(idamAuthApi.getUserByUserId(AUTH_TOKEN, "userId"))
            .thenReturn(UserDetails.builder().id("userId").email("email@email.com").build());

        UserDetails userDetails = idamAuthService.getUserByUserId(AUTH_TOKEN, "userId");

        assertThat(userDetails.getId()).isEqualTo("userId");
    }

    @Test
    void givenToken_whenGetUserInfo_ThenReturnUserInfo() {
        when(idamOidcApi.retrieveUserInfo(AUTH_TOKEN))
            .thenReturn(UserInfo.builder().uid("uidTest").build());

        UserInfo userInfo = idamAuthService.getUserInfo(AUTH_TOKEN);

        assertThat(userInfo.getUid()).isEqualTo("uidTest");
    }

    @Test
    void givenToken_whenGetUserDetails_ThenReturnUserDetails() {
        when(idamOidcApi.retrieveUserInfo(AUTH_TOKEN))
            .thenReturn(UserInfo.builder().uid("uidTest").build());

        UserDetails userDetails = idamAuthService.getUserDetails(AUTH_TOKEN);

        assertThat(userDetails.getId()).isEqualTo("uidTest");
    }

    @Test
    void givenToken_whenGetIdamToken_ThenReturnIdamToken() {
        UserInfo userInfo = UserInfo.builder()
            .uid("uidTest")
            .build();

        when(idamOidcApi.retrieveUserInfo(AUTH_TOKEN)).thenReturn(userInfo);
        when(authTokenGenerator.generate()).thenReturn("service-auth-token");

        IdamToken idamToken = idamAuthService.getIdamToken(AUTH_TOKEN);

        assertThat(idamToken.getUserId()).isEqualTo("uidTest");
    }
}
