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
    @SpringBootTest(properties = {"feature.toggle.approved_consent_order_notification_letter=true"})
    public static class ApprovedConsentOrderNotificationSwitchedOn {

        @Autowired
        private FeatureToggleService classToTest;

        @Test
        public void isApprovedConsentOrderNotificationLetterEnabledReturnsTrue() {
            assertThat(classToTest.isApprovedConsentOrderNotificationLetterEnabled(), is(true));
        }

        @Test
        public void isHwfSuccessfulNotificationLetterEnabledReturnTrue() {
            assertThat(classToTest.isHwfSuccessfulNotificationLetterEnabled(), is(false));
        }

        @Test
        public void isAssignedToJudgeNotificationLetterEnabledReturnTrue() {
            assertThat(classToTest.isAssignedToJudgeNotificationLetterEnabled(), is(false));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationEmptyWhenFeaturesEnabled() {
            assertThat(classToTest.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {"feature.toggle.approved_consent_order_notification_letter=false"})
    public static class ApprovedConsentOrderNotificationSwitchedOff {

        @Autowired
        private FeatureToggleService classToTest;

        @Test
        public void isApprovedConsentOrderNotificationLetterEnabledReturnsFalse() {
            assertThat(classToTest.isApprovedConsentOrderNotificationLetterEnabled(), is(false));
        }

        @Test
        public void isHwfSuccessfulNotificationLetterEnabledReturnTrue() {
            assertThat(classToTest.isHwfSuccessfulNotificationLetterEnabled(), is(false));
        }

        @Test
        public void isAssignedToJudgeNotificationLetterEnabledReturnTrue() {
            assertThat(classToTest.isAssignedToJudgeNotificationLetterEnabled(), is(false));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            assertThat(classToTest.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }
    }
}
