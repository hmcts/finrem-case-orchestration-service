package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SystemUpdateUserConfiguration {
    private final String userName;
    private final String password;

    public SystemUpdateUserConfiguration(@Value("${finrem.system_update.username}") String userName,
                                         @Value("${finrem.system_update.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
