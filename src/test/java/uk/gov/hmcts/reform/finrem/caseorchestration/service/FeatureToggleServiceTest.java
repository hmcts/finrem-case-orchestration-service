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
        "feature.toggle.approved_consent_order_notification_letter=true",
        "feature.toggle.hwf_successful_notification_letter=true",
        "feature.toggle.assigned_to_judge_notification_letter=true",
        "feature.toggle.consent_order_not_approved_applicant_document_generation=true",
        "feature.toggle.print_general_letter=true"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOn {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isApprovedConsentOrderNotificationLetterEnabledReturnsTrue() {
            assertThat(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled(), is(true));
        }

        @Test
        public void isHwfSuccessfulNotificationLetterEnabledReturnTrue() {
            assertThat(featureToggleService.isHwfSuccessfulNotificationLetterEnabled(), is(true));
        }

        @Test
        public void isAssignedToJudgeNotificationLetterEnabledReturnTrue() {
            assertThat(featureToggleService.isAssignedToJudgeNotificationLetterEnabled(), is(true));
        }

        @Test
        public void isConsentOrderNotApprovedApplicantDocumentGenerationEnabledReturnTrue() {
            assertThat(featureToggleService.isConsentOrderNotApprovedApplicantDocumentGenerationEnabled(), is(true));
        }

        @Test
        public void isPrintGeneralLetterEnabledReturnTrue() {
            assertThat(featureToggleService.isPrintGeneralLetterEnabled(), is(true));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationEmptyWhenFeaturesEnabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.approved_consent_order_notification_letter=false",
        "feature.toggle.hwf_successful_notification_letter=false",
        "feature.toggle.assigned_to_judge_notification_letter=false",
        "feature.toggle.consent_order_not_approved_applicant_document_generation=false",
        "feature.toggle.print_general_letter=false"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOff {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isApprovedConsentOrderNotificationLetterEnabledReturnsFalse() {
            assertThat(featureToggleService.isApprovedConsentOrderNotificationLetterEnabled(), is(false));
        }

        @Test
        public void isHwfSuccessfulNotificationLetterEnabledReturnFalse() {
            assertThat(featureToggleService.isHwfSuccessfulNotificationLetterEnabled(), is(false));
        }

        @Test
        public void isAssignedToJudgeNotificationLetterEnabledReturnFalse() {
            assertThat(featureToggleService.isAssignedToJudgeNotificationLetterEnabled(), is(false));
        }

        @Test
        public void isConsentOrderNotApprovedApplicantDocumentGenerationEnabledReturnFalse() {
            assertThat(featureToggleService.isConsentOrderNotApprovedApplicantDocumentGenerationEnabled(), is(false));
        }

        @Test
        public void isPrintGeneralLetterEnabledReturnTrue() {
            assertThat(featureToggleService.isPrintGeneralLetterEnabled(), is(false));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }
    }
}
