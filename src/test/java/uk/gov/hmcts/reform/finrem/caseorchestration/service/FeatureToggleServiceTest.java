package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.core.Is.is;

@RunWith(Enclosed.class)
public class FeatureToggleServiceTest {

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.respondent_solicitor_email_notification=true",
        "feature.toggle.contested_print_draft_order_not_approved=true",
        "feature.toggle.contested_print_general_order=true",
        "feature.toggle.share_a_case=true"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOn {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isRespondentSolicitorEmailNotificationEnabledReturnsTrue() {
            assertThat(featureToggleService.isRespondentSolicitorEmailNotificationEnabled(), is(true));
        }

        @Test
        public void isContestedPrintGeneralOrderEnabledReturnsTrue() {
            assertThat(featureToggleService.isContestedPrintGeneralOrderEnabled(), is(true));
        }

        @Test
        public void isContestedPrintDraftOrderNotApprovedEnabledReturnsTrue() {
            assertThat(featureToggleService.isContestedPrintDraftOrderNotApprovedEnabled(), is(true));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationEmptyWhenFeaturesEnabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }

        @Test
        public void isShareACaseEnabledReturnsTrue() {
            assertThat(featureToggleService.isShareACaseEnabled(), is(true));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.respondent_solicitor_email_notification=false",
        "feature.toggle.contested_print_draft_order_not_approved=false",
        "feature.toggle.contested_print_general_order=false",
        "feature.toggle.send_to_frc=false",
        "feature.toggle.share_a_case=false"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOff {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isRespondentSolicitorEmailNotificationEnabledReturnsFalse() {
            assertThat(featureToggleService.isRespondentSolicitorEmailNotificationEnabled(), is(false));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }

        @Test
        public void isContestedPrintGeneralOrderEnabledReturnsFalse() {
            assertThat(featureToggleService.isContestedPrintGeneralOrderEnabled(), is(false));
        }

        @Test
        public void isContestedPrintDraftOrderNotApprovedEnabledReturnsFalse() {
            assertThat(featureToggleService.isContestedPrintDraftOrderNotApprovedEnabled(), is(false));
        }

        @Test
        public void isSendToFRCEnabledReturnsFalse() {
            assertThat(featureToggleService.isSendToFRCEnabled(), is(false));
        }

        @Test
        public void isShareACaseEnabledReturnsFalse() {
            assertThat(featureToggleService.isShareACaseEnabled(), is(false));
        }
    }
}
