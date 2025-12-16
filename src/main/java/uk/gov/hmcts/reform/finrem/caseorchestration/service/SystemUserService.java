package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling operations related to the system user, such as
 * retrieving tokens and user identifiers.
 *
 * <p>This class is annotated with {@code @Service} to be managed as a Spring bean.
 * It uses Lombok annotations like {@code @Slf4j} for logging and
 * {@code @RequiredArgsConstructor(onConstructor_ = {@Autowired})} for constructor injection of final fields.
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final SystemUserTokenProvider systemUserTokenProvider;
    private final IdamAuthService idamAuthService;

    /**
     * Retrieves the system user token.
     *
     * @return The system user's authentication token as a String.
     */
    public String getSysUserToken() {
        return systemUserTokenProvider.getSysUserToken();
    }

    /**
     * Retrieves the Unique Identifier (UID) of the system user by first getting the system
     * user token and then using it to fetch user information from the IDAM authentication service.
     *
     * @return The UID of the system user as a String.
     */
    public String getSysUserTokenUid() {
        return idamAuthService.getUserInfo(systemUserTokenProvider.getSysUserToken()).getUid();
    }
}
