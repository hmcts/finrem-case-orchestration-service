package uk.gov.hmcts.reform.finrem.caseorchestration.utils.aac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.client.ApproverIdamApi;
import uk.gov.hmcts.reform.finrem.caseorchestration.config.AacApproverOAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserRequest;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;

import java.util.Base64;

@Service
@Slf4j
public class AacApproverIdamClient {
    public static final String AUTH_TYPE = "code";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String BASIC_AUTH_TYPE = "Basic";
    public static final String BEARER_AUTH_TYPE = "Bearer";
    public static final String CODE = "code";

    private ApproverIdamApi approverIdamApi;
    private AacApproverOAuth2Configuration oauth2Configuration;

    @Autowired
    public AacApproverIdamClient(ApproverIdamApi approverIdamApi,
                                 AacApproverOAuth2Configuration oauth2Configuration) {
        this.approverIdamApi = approverIdamApi;
        this.oauth2Configuration = oauth2Configuration;

    }

    public String authenticateUser(String username, String password) {
        String authorisation = username + ":" + password;
        String base64Authorisation = Base64.getEncoder().encodeToString(authorisation.getBytes());

        String clientId = oauth2Configuration.getClientId();

        String redirectUri = oauth2Configuration.getRedirectUri();

        log.info("In authenticate approver, Authorisation = {}", authorisation);
        log.info("In authenticate apptover, clientId = {} : redirect-uri = {}", clientId, redirectUri);

        AuthenticateUserResponse authenticateUserResponse = approverIdamApi.authenticateUser(
            BASIC_AUTH_TYPE + " " + base64Authorisation,
            new AuthenticateUserRequest(AUTH_TYPE, clientId, redirectUri),
            "openid%20profile%20roles%20prd-admin"
        );

        log.info("Got authenticate UserResponse from IdamApi: {}", authenticateUserResponse);

        ExchangeCodeRequest exchangeCodeRequest = new ExchangeCodeRequest(authenticateUserResponse
            .getCode(), GRANT_TYPE, redirectUri, clientId, oauth2Configuration.getClientSecret());

        log.info("Got exchange code request {}", exchangeCodeRequest);

        TokenExchangeResponse tokenExchangeResponse = approverIdamApi.exchangeCode(exchangeCodeRequest);

        log.info("Got token exchange response from IdamApi {}", tokenExchangeResponse);

        return BEARER_AUTH_TYPE + " " + tokenExchangeResponse.getAccessToken();
    }
}
