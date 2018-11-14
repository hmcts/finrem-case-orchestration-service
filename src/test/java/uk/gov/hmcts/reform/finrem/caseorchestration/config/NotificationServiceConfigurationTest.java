package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.CaseOrchestrationApplication;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CaseOrchestrationApplication.class)
@TestPropertySource(locations = "/application.properties")
public class NotificationServiceConfigurationTest {

    @Autowired
    private NotificationServiceConfiguration underTest;

    @Test
    public void shouldReturnTheConfiguration() {
        assertEquals("/notify", underTest.getApi());
        assertEquals("/hwfSuccessful", underTest.getHwfSuccessful());
        assertEquals("http://localhost:8086/", underTest.getUrl());
    }
}