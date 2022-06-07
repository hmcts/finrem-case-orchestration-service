package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.IdamAuthApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.idam.client.IdamClient.BEARER_AUTH_TYPE;
import static uk.gov.hmcts.reform.idam.client.IdamClient.OPENID_GRANT_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdamAuthService {
    private final IdamAuthApi idamAuthApi;
    private final OAuth2Configuration oAuth2Configuration;

    public String getAccessToken(String username, String password) {
        return BEARER_AUTH_TYPE + " " + idamAuthApi.generateOpenIdToken(buildTokenRequest(username, password)).accessToken;
    }

    public UserDetails getUserByUserId(String authorisation, String userId) {
        return idamAuthApi.getUserByUserId(authorisation, userId);
    }

    public UserInfo getUserInfo(String bearerToken) {
        return idamAuthApi.retrieveUserInfo(bearerToken);
    }

    private TokenRequest buildTokenRequest(String username, String password) {
        return new TokenRequest(
            oAuth2Configuration.getClientId(),
            oAuth2Configuration.getClientSecret(),
            OPENID_GRANT_TYPE,
            oAuth2Configuration.getRedirectUri(),
            username,
            password,
            oAuth2Configuration.getClientScope(),
            null,
            null
        );
    }
}
