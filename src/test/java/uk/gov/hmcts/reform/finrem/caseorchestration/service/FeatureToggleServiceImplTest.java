package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.Features;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.impl.FeatureToggleServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FeatureToggleServiceImplTest {

    private FeatureToggleServiceImpl classToTest = new FeatureToggleServiceImpl();

    @Before
    public void setup() {
        Map<String, String> toggles = new HashMap<>();
        toggles.put(Features.APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER.getName(), "false");
        toggles.put(Features.HWF_SUCCESSFUL_NOTIFICATION_LETTER.getName(), "true");

        ReflectionTestUtils.setField(classToTest, "toggle", toggles);
    }

    @Test
    public void givenToggleEnabled_thenReturnTrue() {
        assertThat(classToTest.isFeatureEnabled(Features.HWF_SUCCESSFUL_NOTIFICATION_LETTER), is(true));
    }

    @Test
    public void givenToggleFalse_thenReturnFalse() {
        assertThat(classToTest.isFeatureEnabled(Features.APPROVED_CONSENT_ORDER_NOTIFICATION_LETTER), is(false));
    }
}
