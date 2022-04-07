package uk.gov.hmcts.reform.finrem.caseorchestration.utils.aac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AacApproverAuthUtil {

    @Value("${idam.approver.username}")
    private String approverUserName;

    @Value("${idam.approver.password}")
    private String approverPassword;

    private final AacApproverIdamClient idamClient;

    public String getApproverToken() {
        return getIdamOauth2Token(approverUserName, approverPassword);
    }

    private String getIdamOauth2Token(String username, String password) {
        return idamClient.authenticateUser(username, password);
    }
}
