package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.LocalEmailService;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Disabled
@ActiveProfiles("local")
@SpringBootTest
class LocalProfileTest {

    @Autowired
    LocalEmailService localEmailService;

    @Test
    @Ignore
    void emailServiceShouldBeInstanceOfLocalEmailService() {
        // localEmailService inherits from EmailService, hence the class check.
        assertInstanceOf(LocalEmailService.class, localEmailService);
    }
}
