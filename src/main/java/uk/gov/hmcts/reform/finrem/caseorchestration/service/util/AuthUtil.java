package uk.gov.hmcts.reform.finrem.caseorchestration.service.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static uk.gov.hmcts.reform.idam.client.IdamClient.BEARER_AUTH_TYPE;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtil {

    private static final String BEARER = BEARER_AUTH_TYPE + " ";

    public static String getBearerToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }
}

