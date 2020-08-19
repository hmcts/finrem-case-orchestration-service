package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.finrem.caseorchestration.BaseServiceTest;

import static org.junit.Assert.assertEquals;

public class DefaultsConfigurationTest extends BaseServiceTest {

    @Autowired
    private DefaultsConfiguration underTest;

    @Test
    public void shouldReturnTheConfiguration() {
        assertEquals("new_application@mailinator.com", underTest.getAssignedToJudgeDefault());
    }
}