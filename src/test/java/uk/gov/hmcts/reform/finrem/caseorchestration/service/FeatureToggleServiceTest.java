package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class FeatureToggleServiceTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.automate_send_order=true"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOn {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isAutomateSendOrderEnabledReturnTrue() {
            assertThat(featureToggleService.isAutomateSendOrderEnabled(), is(true));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationEmptyWhenFeaturesEnabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.automate_send_order=false"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOff {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }

        @Test
        public void isAutomateSendOrderEnabledReturnFalse() {
            assertThat(featureToggleService.isAutomateSendOrderEnabled(), is(false));
        }
    }
}
