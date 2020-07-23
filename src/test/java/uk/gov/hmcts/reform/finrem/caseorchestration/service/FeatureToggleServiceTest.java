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
        "feature.toggle.consent_order_not_approved_applicant_document_generation=true",
        "feature.toggle.print_general_letter=true",
        "feature.toggle.print_general_order=true"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOn {

        @Autowired
        private FeatureToggleService featureToggleService;

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

        @Test
        public void isPrintGeneralOrderEnabledReturnTrue() {
            assertThat(featureToggleService.isPrintGeneralOrderEnabled(), is(true));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.consent_order_not_approved_applicant_document_generation=false",
        "feature.toggle.print_general_letter=false",
        "feature.toggle.print_general_order=false"
    })
    public static class ApprovedConsentOrderNotificationSwitchedOff {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        public void isConsentOrderNotApprovedApplicantDocumentGenerationEnabledReturnFalse() {
            assertThat(featureToggleService.isConsentOrderNotApprovedApplicantDocumentGenerationEnabled(), is(false));
        }

        @Test
        public void isPrintGeneralLetterEnabledReturnFalse() {
            assertThat(featureToggleService.isPrintGeneralLetterEnabled(), is(false));
        }

        @Test
        public void getFieldsIgnoredDuringSerialisationContainsElementsWhenFeaturesDisabled() {
            assertThat(featureToggleService.getFieldsIgnoredDuringSerialisation(), is(anEmptyMap()));
        }

        @Test
        public void isPrintGeneralOrderEnabledReturnFalse() {
            assertThat(featureToggleService.isPrintGeneralOrderEnabled(), is(false));
        }
    }
}
