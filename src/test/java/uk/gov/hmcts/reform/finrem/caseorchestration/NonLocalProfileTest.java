package uk.gov.hmcts.reform.finrem.caseorchestration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.EmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.service.LocalEmailService;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

// Profile not specified, so not 'local'
@SpringBootTest
class NonLocalProfileTest {

    @Autowired(required = false)
    EmailService emailService;

    @Test
    void emailServiceShouldBeInstanceOfLocalEmailService() {
        // Check that an EmailService is loaded into context. LocalEmailService should
        // only be used when the 'local' profile is active.
        assertNotEquals(
                LocalEmailService.class,
                emailService.getClass(),
                "Expected emailService to not be LocalEmailService"
        );
    }
}
