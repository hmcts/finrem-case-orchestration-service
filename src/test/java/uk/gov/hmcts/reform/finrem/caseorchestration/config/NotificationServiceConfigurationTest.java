package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.junit.Assert.assertEquals;

public class NotificationServiceConfigurationTest extends BaseServiceTest {

    @Autowired
    private NotificationServiceConfiguration underTest;

    @Test
    public void shouldReturnTheConfiguration() {
        assertEquals("fr_applicant_sol@sharklasers.com", underTest.getCtscEmail());
    }
}