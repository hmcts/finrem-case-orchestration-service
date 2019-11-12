package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.ForbiddenException;
import uk.gov.hmcts.reform.finrem.caseorchestration.error.UnauthenticatedException;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class AuthService {

    private final AuthTokenValidator authTokenValidator;
    private final List<String> allowedServices;

    public AuthService(
            AuthTokenValidator authTokenValidator,
            @Value("${allowed-services}") List<String> allowedServices
    ) {
        this.authTokenValidator = authTokenValidator;
        this.allowedServices = allowedServices;
    }

    public String authenticate(String authHeader) {
        if (isBlank(authHeader)) {
            throw new UnauthenticatedException("Provided S2S token is missing or invalid");
        } else {
            return authTokenValidator.getServiceName(authHeader);
        }
    }

    public void assertIsAllowedService(String serviceName) {
        if (!allowedServices.contains(serviceName)) {
            throw new ForbiddenException("S2S token is not authorized to use the service");
        }
    }
}

